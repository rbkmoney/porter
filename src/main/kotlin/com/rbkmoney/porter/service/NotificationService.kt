package com.rbkmoney.porter.service

import com.rbkmoney.notification.NotificationTemplateNotFound
import com.rbkmoney.notification.PartyIdNotFound
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.TotalNotificationProjection
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.PartyStatus
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
class NotificationService(
    private val notificationTemplateRepository: NotificationTemplateRepository,
    private val notificationRepository: NotificationRepository,
    private val partyRepository: PartyRepository,
) {

    @Transactional
    fun createNotifications(templateId: String, partyIds: Collection<String>) {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val parties = partyRepository.findByPartyIdIn(partyIds)
        val notificationEntities = partyIds.map { partyId ->
            NotificationEntity().apply {
                this.notificationTemplateEntity = notificationTemplateEntity
                this.partyEntity = parties.find { it.partyId == partyId }
                    ?: throw PartyIdNotFound(partyId)
                this.notificationId = IdGenerator.randomString()
            }
        }
        notificationRepository.saveAll(notificationEntities)
        notificationTemplateEntity.notifications.addAll(notificationEntities)
    }

    @Transactional
    fun createNotifications(templateId: String) {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val notificationEntities = partyRepository.findAllByStatus(PartyStatus.active).map { partyEntity ->
            NotificationEntity().apply {
                this.notificationTemplateEntity = notificationTemplateEntity
                this.partyEntity = partyEntity
                this.notificationId = IdGenerator.randomString()
            }
        }.collect(Collectors.toList())
        notificationRepository.saveAll(notificationEntities)
    }

    fun findNotifications(
        filter: NotificationFilter,
        continuationToken: ContinuationToken? = null,
        limit: Int = 10,
    ): Page<NotificationEntity> {
        return if (continuationToken != null) {
            notificationRepository.findNextNotifications(continuationToken = continuationToken, limit = limit)
        } else {
            notificationRepository.findNotifications(filter = filter, limit = limit)
        }
    }

    fun findNotificationStats(templateId: String): TotalNotificationProjection {
        val notificationTemplateEntity =
            notificationTemplateRepository.findByTemplateId(templateId) ?: throw NotificationTemplateNotFound()
        return notificationRepository.findNotificationCount(notificationTemplateEntity.id!!)
    }

    fun findNotificationStats(templateId: Long): TotalNotificationProjection {
        return notificationRepository.findNotificationCount(templateId)
    }

    fun getNotification(notificationId: String): NotificationEntity? {
        return notificationRepository.findByNotificationId(notificationId)
    }

    @Transactional
    fun softDeleteNotification(partyId: String, vararg notificationIds: String) {
        notificationRepository.softDeleteAllByPartyIdAndNotificationIdIn(partyId, notificationIds.toList())
    }

    @Transactional
    fun notificationMark(partyId: String, notificationIds: List<String>, status: NotificationStatus) {
        return notificationRepository.markNotifications(partyId, notificationIds, status)
    }

    @Transactional
    fun notificationMarkAll(partyId: String, status: NotificationStatus) {
        return notificationRepository.markAllNotifications(partyId, status)
    }
}
