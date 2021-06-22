package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.pagination.ContinuationToken
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate

@Component
class NotificationRepositoryCustomImpl(
    private val entityManager: EntityManager,
) : NotificationRepositoryCustom {

    override fun findNotifications(
        template: NotificationTemplateEntity,
        status: NotificationStatus?,
        limit: Int,
    ): List<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val notificationEntity = query.from(NotificationEntity::class.java)
        val idPath = notificationEntity.get<Long>("id")
        val createdAtPath = notificationEntity.get<LocalDateTime>("createdAt")
        val templateIdPath = notificationEntity.get<Any>("notificationTemplateEntity")

        val criteriaQuery = query.select(notificationEntity)
            .where(
                cb.and(
                    *cb.let {
                        val predicates = mutableListOf<Predicate>()
                        predicates.add(cb.equal(templateIdPath, template))
                        if (status != null) {
                            val statusPath = notificationEntity.get<NotificationStatus>("status")
                            predicates.add(cb.equal(statusPath, status))
                        }
                        predicates.toTypedArray()
                    }
                )
            ).orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        return entityManager.createQuery(criteriaQuery).setMaxResults(limit).resultList.toList()
    }

    override fun findNotifications(continuationToken: ContinuationToken, limit: Int): List<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val notificationEntity = query.from(NotificationEntity::class.java)
        val idPath = notificationEntity.get<Long>("id")
        val createdAtPath = notificationEntity.get<LocalDateTime>("createdAt")
        val templateIdPath =
            notificationEntity.get<NotificationTemplateEntity>("notificationTemplateEntity").get<String>("templateId")

        val criteriaQuery = query.select(notificationEntity)
            .where(
                cb.and(
                    *cb.let {
                        val predicates = mutableListOf<Predicate>()
                        continuationToken.keyParams?.let { keyParams ->
                            predicates.add(cb.equal(templateIdPath, keyParams["template_id"]))
                            if (keyParams.containsKey("status")) {
                                val statusPath = notificationEntity.get<NotificationStatus>("status")
                                predicates.add(cb.equal(statusPath, NotificationStatus.valueOf(keyParams["status"]!!)))
                            }
                        }
                        val timestamp = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(continuationToken.timestamp),
                            ZoneId.of("UTC")
                        )
                        predicates.add(
                            it.and(
                                it.greaterThanOrEqualTo(createdAtPath, timestamp),
                                it.greaterThan(idPath, continuationToken.id.toLong())
                            )
                        )

                        predicates.toTypedArray()
                    }
                )
            ).orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        return entityManager.createQuery(criteriaQuery).setMaxResults(limit).resultList.toList()
    }
}
