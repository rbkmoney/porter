package com.rbkmoney.porter.handler

import com.rbkmoney.notification.NotificationServiceSrv
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateCreateRequest
import com.rbkmoney.notification.NotificationTemplateModifyRequest
import com.rbkmoney.notification.NotificationTemplatePartyRequest
import com.rbkmoney.notification.NotificationTemplatePartyResponse
import com.rbkmoney.notification.NotificationTemplateSearchRequest
import com.rbkmoney.notification.NotificationTemplateSearchResponse
import com.rbkmoney.porter.service.NotificationService
import mu.KotlinLogging
import java.util.Base64

private val log = KotlinLogging.logger {}

class NotificationServiceHandler(
    private val notificationService: NotificationService
) : NotificationServiceSrv.Iface {

    override fun createNotificationTemplate(
        request: NotificationTemplateCreateRequest,
    ): NotificationTemplate {
        log.info { "Create notification template: $request" }
        return notificationService.createNotificationTemplate(
            title = request.title,
            content = String(Base64.getDecoder().decode(request.content))
        )
    }

    override fun modifyNotificationTemplate(
        request: NotificationTemplateModifyRequest,
    ): NotificationTemplate {
        log.info { "Modify notification template: $request" }
        return notificationService.editNotificationTemplate(request.templateId, request.title, request.content)
    }

    override fun getNotificationTemplate(templateId: String): NotificationTemplate {
        log.info { "Get notification template: templateId=$templateId" }
        return notificationService.getNotificationTemplate(templateId)
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
