package com.rbkmoney.porter.service

import com.rbkmoney.notification.NotificationTemplateNotFound
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.TotalNotificationProjection
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.PartyStatus
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.model.toKeyParams
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.persistence.EntityManager

@Service
class NotificationService(
    private val notificationTemplateRepository: NotificationTemplateRepository,
    private val notificationRepository: NotificationRepository,
    private val partyRepository: PartyRepository,
    private val continuationTokenService: ContinuationTokenService,
    private val entityManager: EntityManager,
) {

    @Transactional
    fun createNotifications(templateId: String, partyIds: MutableList<String>) {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val notificationEntities = partyIds.map { partyId ->
            NotificationEntity().apply {
                this.notificationTemplateEntity = notificationTemplateEntity
                this.partyId = partyId
            }
        }
        notificationRepository.saveAll(notificationEntities)
    }

    @Transactional
    fun createNotifications(templateId: String) {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        partyRepository.findAllByPartyStatus(PartyStatus.active).forEach { party ->
            val notificationEntity = NotificationEntity().apply {
                this.notificationTemplateEntity = notificationTemplateEntity
                this.partyId = party.partyId
                this.notificationId = UUID.randomUUID().toString()
            }
            notificationRepository.save(notificationEntity)
            entityManager.detach(party)
        }
    }

    fun findNotifications(
        templateId: String,
        filter: NotificationFilter? = null,
        continuationToken: ContinuationToken? = null,
        limit: Int = 10,
    ): Page<NotificationEntity> {
        val template = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val notificationEntities = if (continuationToken != null) {
            notificationRepository.findNotifications(continuationToken = continuationToken, limit = limit)
        } else {
            notificationRepository.findNotifications(
                template = template,
                status = filter?.status,
                limit = limit
            )
        }

        val keyParams = HashMap<String, String>().apply {
            put("template_id", templateId)
            filter?.toKeyParams()?.let { params -> putAll(params) }
        }
        val page = continuationTokenService.createPage(notificationEntities, continuationToken, keyParams, limit)

        return page
    }

    fun findNotificationTotal(templateId: String): TotalNotificationProjection {
        val notificationTemplateEntity =
            notificationTemplateRepository.findByTemplateId(templateId) ?: throw NotificationTemplateNotFound()
        return notificationRepository.findNotificationCountTest(
            notificationTemplateEntity.id!!,
            NotificationStatus.read
        )
    }

    fun findNotificationTotal(templateId: Long): TotalNotificationProjection {
        return notificationRepository.findNotificationCountTest(templateId, NotificationStatus.read)
    }
}
