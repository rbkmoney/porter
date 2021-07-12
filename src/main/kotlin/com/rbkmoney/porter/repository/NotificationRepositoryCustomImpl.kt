package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Component
class NotificationRepositoryCustomImpl(
    private val entityManager: EntityManager,
    private val continuationTokenService: ContinuationTokenService,
) : NotificationRepositoryCustom {

    override fun findNotifications(
        template: NotificationTemplateEntity,
        status: NotificationStatus?,
        limit: Int,
    ): Page<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val root = query.from(NotificationEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            add(cb.equal(root.get<Any>("notificationTemplateEntity"), template))
            statusPredicate(cb, root, status)
        }

        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val criteriaQuery = query.select(root)
            .where(*predicates.toTypedArray())
            .orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()
        val keyParams = HashMap<String, String>().apply {
            put("template_id", template.templateId!!)
            status?.let { put("status", status.name) }
        }

        return continuationTokenService.createPage(resultList, null, keyParams, limit + 1)
    }

    override fun findNextNotifications(continuationToken: ContinuationToken, limit: Int): Page<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val root = query.from(NotificationEntity::class.java)
        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val templateIdPath =
            root.get<NotificationTemplateEntity>("notificationTemplateEntity").get<String>("templateId")

        val predicates = mutableListOf<Predicate>().apply {
            continuationToken.keyParams?.let { keyParams ->
                add(cb.equal(templateIdPath, keyParams["template_id"]))
                statusPredicate(cb, root, keyParams["status"])
            }
            add(continuationPredicate(cb, root, continuationToken.timestamp, continuationToken.id.toLong()))
        }

        val criteriaQuery = query.select(root)
            .where(*predicates.toTypedArray())
            .orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()

        return continuationTokenService.createPage(
            resultList,
            continuationToken,
            continuationToken.keyParams,
            limit + 1
        )
    }

    private fun statusPredicate(cb: CriteriaBuilder, root: Root<*>, status: NotificationStatus?): Predicate {
        return if (status != null) {
            cb.equal(root.get<NotificationStatus>("status"), status)
        } else cb.conjunction()
    }

    private fun statusPredicate(cb: CriteriaBuilder, root: Root<*>, status: String?): Predicate {
        return if (status != null) {
            cb.equal(root.get<NotificationStatus>("status"), NotificationStatus.valueOf(status))
        } else cb.conjunction()
    }

    private fun continuationPredicate(cb: CriteriaBuilder, root: Root<*>, fromTimestamp: Long, id: Long): Predicate {
        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(fromTimestamp), ZoneId.of("UTC"))
        return cb.and(
            cb.greaterThanOrEqualTo(createdAtPath, timestamp),
            cb.greaterThan(idPath, id)
        )
    }
}
