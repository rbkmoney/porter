package com.rbkmoney.porter.repository

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Component
class NotificationRepositoryCustomImpl(
    private val entityManager: EntityManager,
    private val continuationTokenService: ContinuationTokenService,
) : NotificationRepositoryCustom {

    override fun findNotifications(filter: NotificationFilter?, limit: Int): Page<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val root = query.from(NotificationEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            add(templatePredicate(cb, root, filter?.templateId))
            add(partyPredicate(cb, root, filter?.partyId))
            add(emailPredicate(cb, root, filter?.email))
            add(titlePredicate(cb, root, filter?.title))
            add(statusPredicate(cb, root, filter?.status))
            add(deletedPredicate(cb, root, filter?.deleted))
            add(fromTimeToTimePredicate(cb, root, filter?.fromTime, filter?.toTime))
        }

        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val criteriaQuery = query.select(root)
            .where(*predicates.toTypedArray())
            .orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()
        val keyParams = HashMap<String, String>().apply {
            filter?.templateId?.let { put(TEMPLATE_ID_PARAM, it) }
            filter?.partyId?.let { put(PARTY_ID_PARAM, it) }
            filter?.email?.let { put(EMAIL_PARAM, it) }
            filter?.title?.let { put(TITLE_PARAM, it) }
            filter?.status?.let { put(STATUS_PARAM, it.name) }
            filter?.deleted?.let { put(DELETED_PARAM, it.toString()) }
            filter?.fromTime?.let { put(FROM_TIME_PARAM, TypeUtil.temporalToString(it)) }
            filter?.toTime?.let { put(TO_TIME_PARAM, TypeUtil.temporalToString(it)) }
        }

        return continuationTokenService.createPage(resultList, null, keyParams, limit + 1)
    }

    override fun findNextNotifications(continuationToken: ContinuationToken, limit: Int): Page<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val root = query.from(NotificationEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            continuationToken.keyParams?.let { keyParams ->
                add(templatePredicate(cb, root, keyParams[TEMPLATE_ID_PARAM]))
                add(partyPredicate(cb, root, keyParams[PARTY_ID_PARAM]))
                add(emailPredicate(cb, root, keyParams[EMAIL_PARAM]))
                add(titlePredicate(cb, root, keyParams[TITLE_PARAM]))
                add(statusPredicate(cb, root, keyParams[STATUS_PARAM]))
                add(deletedPredicate(cb, root, keyParams[DELETED_PARAM]?.let { it.toBoolean() }))
                add(
                    fromTimeToTimePredicate(
                        cb,
                        root,
                        keyParams[FROM_TIME_PARAM]?.let { TypeUtil.stringToLocalDateTime(it) },
                        keyParams[TO_TIME_PARAM]?.let { TypeUtil.stringToLocalDateTime(it) }
                    )
                )
            }
            add(continuationPredicate(cb, root, continuationToken.timestamp, continuationToken.id.toLong()))
        }

        val criteriaQuery = query.select(root)
            .where(*predicates.toTypedArray())
            .orderBy(cb.asc(root.get<Long>("id")), cb.asc(root.get<LocalDateTime>("createdAt")))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()

        return continuationTokenService.createPage(
            resultList,
            continuationToken,
            continuationToken.keyParams,
            limit + 1
        )
    }

    private fun templatePredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, templateId: String?): Predicate {
        return if (templateId != null) {
            val notificationTemplateJoin =
                root.join<NotificationEntity, NotificationTemplateEntity>("notificationTemplateEntity", JoinType.INNER)
            cb.equal(
                notificationTemplateJoin.get<String>("templateId"),
                templateId
            )
        } else cb.conjunction()
    }

    private fun statusPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationEntity>,
        status: NotificationStatus?,
    ): Predicate {
        return if (status != null) {
            cb.equal(root.get<NotificationStatus>("status"), status)
        } else cb.conjunction()
    }

    private fun statusPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, status: String?): Predicate {
        return if (status != null) {
            cb.equal(root.get<NotificationStatus>("status"), NotificationStatus.valueOf(status))
        } else cb.conjunction()
    }

    private fun partyPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, partyId: String?): Predicate {
        return if (partyId != null) {
            val partyEntityJoin = root.join<NotificationEntity, PartyEntity>("partyEntity", JoinType.LEFT)
            cb.equal(partyEntityJoin.get<String>("partyId"), partyId)
        } else cb.conjunction()
    }

    private fun deletedPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, deleted: Boolean?): Predicate {
        return if (deleted != null) {
            cb.equal(root.get<Boolean>("deleted"), deleted)
        } else cb.conjunction()
    }

    private fun fromTimeToTimePredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationEntity>,
        fromTime: LocalDateTime?,
        toTime: LocalDateTime?,
    ): Predicate {
        return if (fromTime != null && toTime != null) {
            val createdAtPath = root.get<LocalDateTime>("createdAt")
            cb.and(cb.greaterThanOrEqualTo(createdAtPath, fromTime), cb.lessThanOrEqualTo(createdAtPath, toTime))
        } else cb.conjunction()
    }

    private fun titlePredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, title: String?): Predicate {
        return if (title != null) {
            val notificationTemplateJoin =
                root.join<NotificationEntity, NotificationTemplateEntity>("notificationTemplateEntity", JoinType.INNER)
            val searchedTitle = "%${title.lowercase()}%"
            cb.like(
                cb.lower(notificationTemplateJoin.get<String>("title")),
                searchedTitle
            )
        } else cb.conjunction()
    }

    private fun emailPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, email: String?): Predicate {
        return if (email != null) {
            val notificationTemplateJoin =
                root.join<NotificationEntity, PartyEntity>("partyEntity", JoinType.INNER)
            cb.equal(notificationTemplateJoin.get<String>("email"), email)
        } else cb.conjunction()
    }

    private fun continuationPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationEntity>,
        fromTimestamp: Long,
        id: Long,
    ): Predicate {
        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(fromTimestamp), ZoneId.of("UTC"))
        return cb.and(
            cb.greaterThanOrEqualTo(createdAtPath, timestamp),
            cb.greaterThan(idPath, id)
        )
    }

    private companion object KeyParams {
        const val TEMPLATE_ID_PARAM = "template_id"
        const val PARTY_ID_PARAM = "party_id"
        const val EMAIL_PARAM = "email"
        const val TITLE_PARAM = "title"
        const val STATUS_PARAM = "status"
        const val DELETED_PARAM = "deleted"
        const val FROM_TIME_PARAM = "from_time"
        const val TO_TIME_PARAM = "to_time"
    }
}
