package com.rbkmoney.porter.service

import com.rbkmoney.notification.BadNotificationTemplateState
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateNotFound
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class NotificationTemplateService(
    private val conversionService: ConversionService,
    private val notificationRepository: NotificationRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
    private val partyRepository: PartyRepository,
) {

    fun createNotificationTemplate(title: String, content: String): NotificationTemplate {
        val notificationTemplateEntity = notificationTemplateRepository.save(
            NotificationTemplateEntity().apply {
                templateId = UUID.randomUUID().toString()
                createdAt = LocalDateTime.now()
                this.title = title
                this.content = content
                status = NotificationTemplateStatus.draft
            }
        )
        val notificationTemplateEntityEnriched = NotificationTemplateEntityEnriched(notificationTemplateEntity)

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
    }

    @Transactional
    fun editNotificationTemplate(templateId: String, title: String?, content: String?): NotificationTemplate {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        if (notificationTemplateEntity.status == NotificationTemplateStatus.final) {
            throw BadNotificationTemplateState(
                "You can't modify notification template (${notificationTemplateEntity.templateId}) in final state"
            )
        }

        notificationTemplateEntity.title = title ?: notificationTemplateEntity.title
        notificationTemplateEntity.content = content ?: notificationTemplateEntity.content
        notificationTemplateEntity.updatedAt = LocalDateTime.now()

        val editedNotificationTemplate = notificationTemplateRepository.save(notificationTemplateEntity)
        val notificationTemplateEntityEnriched =
            NotificationTemplateEntityEnriched(editedNotificationTemplate) // TODO: add readCount & totalCount

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
    }

    fun getNotificationTemplate(templateId: String): NotificationTemplate {
        val notificationTemplate = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        val notificationTemplateEntityEnriched =
            NotificationTemplateEntityEnriched(notificationTemplate) // TODO: add readCount & totalCount

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
    }
}
