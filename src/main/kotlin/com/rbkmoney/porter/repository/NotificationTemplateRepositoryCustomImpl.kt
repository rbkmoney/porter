package com.rbkmoney.porter.repository

import com.rbkmoney.geck.common.util.TypeUtil
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
class NotificationTemplateRepositoryCustomImpl(
    private val entityManager: EntityManager,
) : NotificationTemplateRepositoryCustom {

    override fun findNotificationTemplates(
        from: LocalDateTime,
        to: LocalDateTime?,
        title: String?,
        content: String?,
        date: LocalDateTime?,
        limit: Int,
    ): List<NotificationTemplateEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationTemplateEntity::class.java)
        val notificationTemplateEntity = query.from(NotificationTemplateEntity::class.java)
        val idPath = notificationTemplateEntity.get<Long>("id")
        val createdAtPath = notificationTemplateEntity.get<LocalDateTime>("createdAt")

        val criteriaQuery = query.select(notificationTemplateEntity)
            .where(
                cb.and(
                    *cb.let {
                        val predicates = mutableListOf<Predicate>()
                        if (title != null) {
                            val titlePath = notificationTemplateEntity.get<String>("title")
                            predicates.add(it.and(cb.equal(titlePath, title)))
                        }
                        if (content != null) {
                            val contentPath = notificationTemplateEntity.get<String>("content")
                            predicates.add(it.and(cb.like(cb.lower(contentPath), "%${content.lowercase()}%")))
                        }
                        if (date != null) {
                            val updatedAtPath = notificationTemplateEntity.get<LocalDateTime>("updatedAt")
                            predicates.add(it.or(cb.equal(createdAtPath, date), cb.equal(updatedAtPath, date)))
                        }
                        predicates.add(it.greaterThanOrEqualTo(createdAtPath, from))
                        if (to != null) {
                            predicates.add(it.lessThanOrEqualTo(createdAtPath, to))
                        }

                        predicates.toTypedArray()
                    }
                )
            ).orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        return entityManager.createQuery(criteriaQuery).setMaxResults(limit).resultList.toList()
    }

    override fun findNextNotificationTemplates(
        continuationToken: ContinuationToken,
        limit: Int,
    ): List<NotificationTemplateEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationTemplateEntity::class.java)
        val notificationTemplateEntity = query.from(NotificationTemplateEntity::class.java)
        val idPath = notificationTemplateEntity.get<Long>("id")
        val createdAtPath = notificationTemplateEntity.get<LocalDateTime>("createdAt")

        val criteriaQuery = query.select(notificationTemplateEntity)
            .where(
                cb.and(
                    *cb.let {
                        val predicates = mutableListOf<Predicate>()
                        continuationToken.keyParams?.let { keyParams ->
                            if (keyParams.containsKey("title")) {
                                val titlePath = notificationTemplateEntity.get<String>("title")
                                predicates.add(cb.equal(titlePath, continuationToken.keyParams["title"]))
                            }
                            if (keyParams.containsKey("content")) {
                                val contentPath = notificationTemplateEntity.get<String>("content")
                                val searchedText =
                                    "%${continuationToken.keyParams["content"]?.toString()?.lowercase()}%"
                                predicates.add(cb.like(contentPath, searchedText))
                            }
                            if (keyParams.containsKey("date")) {
                                val updatedAtPath = notificationTemplateEntity.get<LocalDateTime>("updatedAt")
                                val date = TypeUtil.stringToLocalDateTime(continuationToken.keyParams["date"])
                                predicates.add(it.or(cb.equal(createdAtPath, date), cb.equal(updatedAtPath, date)))
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
                        if (continuationToken.keyParams?.containsKey("to") == true) {
                            val toDate = TypeUtil.stringToLocalDateTime(continuationToken.keyParams["to"])
                            predicates.add(it.lessThanOrEqualTo(createdAtPath, toDate))
                        }

                        predicates.toTypedArray()
                    }
                )
            ).orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        return entityManager.createQuery(criteriaQuery).setMaxResults(limit).resultList.toList()
    }
}
