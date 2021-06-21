package com.rbkmoney.porter.handler

import com.rbkmoney.notification.NotificationServiceSrv
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateCreateRequest
import com.rbkmoney.notification.NotificationTemplateModifyRequest
import com.rbkmoney.notification.NotificationTemplatePartyRequest
import com.rbkmoney.notification.NotificationTemplatePartyResponse
import com.rbkmoney.notification.NotificationTemplateSearchRequest
import com.rbkmoney.notification.NotificationTemplateSearchResponse
import com.rbkmoney.notification.base.InvalidRequest
import com.rbkmoney.porter.service.NotificationTemplateService
import mu.KotlinLogging
import java.util.Base64

private val log = KotlinLogging.logger {}

class NotificationServiceHandler(
    private val notificationTemplateService: NotificationTemplateService,
) : NotificationServiceSrv.Iface {

    override fun createNotificationTemplate(
        request: NotificationTemplateCreateRequest,
    ): NotificationTemplate {
        if (!org.apache.commons.codec.binary.Base64.isBase64(request.content)) {
            throw InvalidRequest(listOf("Expected base64 'content' format"))
        }
        log.info { "Create notification template request: $request" }
        val notificationTemplate = notificationTemplateService.createNotificationTemplate(
            title = request.title,
            content = String(Base64.getDecoder().decode(request.content))
        )
        log.info { "Create notification template result: $notificationTemplate" }

        return notificationTemplate
    }

    override fun modifyNotificationTemplate(
        request: NotificationTemplateModifyRequest,
    ): NotificationTemplate {
        log.info { "Modify notification template request: $request" }
        val notificationTemplate =
            notificationTemplateService.editNotificationTemplate(request.templateId, request.title, request.content)
        log.info { "Modify notification template result: $notificationTemplate" }

        return notificationTemplate
    }

    override fun getNotificationTemplate(templateId: String): NotificationTemplate {
        log.info { "Get notification template by templateId=$templateId" }
        val notificationTemplate = notificationTemplateService.getNotificationTemplate(templateId)
        log.info { "Get notification template result: $notificationTemplate" }

        return notificationTemplate
    }

    override fun findNotificationTemplateParties(
        request: NotificationTemplatePartyRequest,
    ): NotificationTemplatePartyResponse {
        TODO("Not yet implemented")
    }

    override fun findNotificationTemplates(
        request: NotificationTemplateSearchRequest,
    ): NotificationTemplateSearchResponse {
        TODO("Not yet implemented")
    }

    override fun sendNotification(tempalteId: String, partyIds: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun sendNotificationAll(tempalteId: String) {
        TODO("Not yet implemented")
    }
}
