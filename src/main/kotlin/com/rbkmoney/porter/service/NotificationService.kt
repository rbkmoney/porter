package com.rbkmoney.porter.service

import com.rbkmoney.notification.NotificationTemplateNotFound
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.TotalNotificationProjection
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.stream.Collectors
import javax.persistence.EntityManager

@Service
class NotificationService(
    private val notificationTemplateRepository: NotificationTemplateRepository,
    private val notificationRepository: NotificationRepository,
    private val partyRepository: PartyRepository,
    private val entityManager: EntityManager,
) {

    fun createNotifications(templateId: String, partyIds: MutableList<String>) {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val notificationEntities = partyIds.map { partyId ->
            NotificationEntity().apply {
                this.notificationTemplateEntity = notificationTemplateEntity
                this.partyId = partyId
                this.notificationId = UUID.randomUUID().toString()
            }
        }
        notificationRepository.saveAll(notificationEntities)
    }

    @Transactional
    fun createNotifications(templateId: String) {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val notificationEntities = partyRepository.findAllByPartyStatus(PartyStatus.active).map {
            NotificationEntity().apply {
                this.notificationTemplateEntity = notificationTemplateEntity
                this.partyId = it.partyId
                this.notificationId = UUID.randomUUID().toString()
            }
        }.collect(Collectors.toList())
        notificationRepository.saveAll(notificationEntities)
    }

    fun findNotifications(
        filter: NotificationFilter,
        continuationToken: ContinuationToken? = null,
        limit: Int = 10,
    ): Page<NotificationEntity> {
        val template = notificationTemplateRepository.findByTemplateId(filter.templateId)
            ?: throw NotificationTemplateNotFound()
        return if (continuationToken != null) {
            notificationRepository.findNextNotifications(continuationToken = continuationToken, limit = limit)
        } else {
            notificationRepository.findNotifications(
                template = template,
                status = filter.status,
                limit = limit
            )
        }
    }

    fun findNotificationTotal(templateId: String): TotalNotificationProjection {
        val notificationTemplateEntity =
            notificationTemplateRepository.findByTemplateId(templateId) ?: throw NotificationTemplateNotFound()
        return notificationRepository.findNotificationCount(notificationTemplateEntity.id!!)
    }

    fun findNotificationTotal(templateId: Long): TotalNotificationProjection {
        return notificationRepository.findNotificationCount(templateId)
    }
}
