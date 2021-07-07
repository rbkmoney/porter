package com.rbkmoney.porter.handler

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.NotificationServiceSrv
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateCreateRequest
import com.rbkmoney.notification.NotificationTemplateModifyRequest
import com.rbkmoney.notification.NotificationTemplatePartyRequest
import com.rbkmoney.notification.NotificationTemplatePartyResponse
import com.rbkmoney.notification.NotificationTemplateSearchRequest
import com.rbkmoney.notification.NotificationTemplateSearchResponse
import com.rbkmoney.notification.PartyNotification
import com.rbkmoney.notification.base.InvalidRequest
import com.rbkmoney.porter.converter.model.NotificationEntityEnriched
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.service.NotificationSenderService
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.NotificationTemplateService
import com.rbkmoney.porter.service.PartyService
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.model.NotificationTemplateFilter
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import mu.KotlinLogging
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import java.util.Base64

private val log = KotlinLogging.logger {}

@Service
class NotificationServiceHandler(
    private val notificationTemplateService: NotificationTemplateService,
    private val notificationService: NotificationService,
    private val notificationSenderService: NotificationSenderService,
    private val conversionService: ConversionService,
    private val continuationTokenService: ContinuationTokenService,
    private val partyService: PartyService,
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
        val notificationTemplateEntityEnriched = NotificationTemplateEntityEnriched(notificationTemplate)
        log.info { "Create notification template result: $notificationTemplate" }

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
    }

    override fun modifyNotificationTemplate(
        request: NotificationTemplateModifyRequest,
    ): NotificationTemplate {
        log.info { "Modify notification template request: $request" }
        val notificationTemplate =
            notificationTemplateService.editNotificationTemplate(request.templateId, request.title, request.content)
        val notificationStats = notificationService.findNotificationStats(notificationTemplate.templateId!!)
        val notificationTemplateEntityEnriched =
            NotificationTemplateEntityEnriched(notificationTemplate, notificationStats.read, notificationStats.total)
        log.info { "Modify notification template result: $notificationTemplate" }

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
    }

    override fun getNotificationTemplate(templateId: String): NotificationTemplate {
        log.info { "Get notification template by templateId=$templateId" }
        val notificationTemplate = notificationTemplateService.getNotificationTemplate(templateId)
        val notificationStats = notificationService.findNotificationStats(notificationTemplate.templateId!!)
        val notificationTemplateEntityEnriched =
            NotificationTemplateEntityEnriched(notificationTemplate, notificationStats.read, notificationStats.total)
        log.info { "Get notification template result: $notificationTemplate" }

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
    }

    override fun findNotificationTemplateParties(
        request: NotificationTemplatePartyRequest,
    ): NotificationTemplatePartyResponse {
        log.info { "Find notification template request: $request" }
        val continuationToken = request.continuation_token?.let {
            continuationTokenService.tokenFromString(request.continuation_token)
        }
        val notificationFilter = NotificationFilter(
            templateId = request.template_id,
            status = conversionService.convert(request.status, NotificationStatus::class.java)
        )
        val page = notificationService.findNotifications(
            filter = notificationFilter,
            continuationToken = continuationToken,
            limit = request.limit
        )

        return NotificationTemplatePartyResponse().apply {
            continuation_token = if (page.hasNext)
                continuationTokenService.tokenToString(page.token!!) else null
            parties = page.entities.map {
                val partyName = partyService.getPartyName(it.partyId!!)
                val notificationEntityEnriched = NotificationEntityEnriched(it, partyName)
                conversionService.convert(notificationEntityEnriched, PartyNotification::class.java)!!
            }
        }
    }

    override fun findNotificationTemplates(
        request: NotificationTemplateSearchRequest,
    ): NotificationTemplateSearchResponse {
        val notificationTemplateFilter = NotificationTemplateFilter(
            title = request.title,
            content = request.content,
            from = if (request.date.isSetRangeDateFilter) TypeUtil.stringToLocalDateTime(request.date.rangeDateFilter.fromDate) else null,
            to = if (request.date.isSetRangeDateFilter) TypeUtil.stringToLocalDateTime(request.date.rangeDateFilter.toDate) else null,
            date = if (request.date.isSetFixedDateFilter) TypeUtil.stringToLocalDateTime(request.date.fixedDateFilter.date) else null
        )
        val token: String? = request.continuation_token
        val continuationToken = token?.let { continuationTokenService.tokenFromString(token) }

        val notificationTemplatesPage = notificationTemplateService.findNotificationTemplate(
            continuationToken = continuationToken,
            filter = notificationTemplateFilter,
            limit = request.limit
        )

        return NotificationTemplateSearchResponse().apply {
            continuation_token = if (notificationTemplatesPage.hasNext)
                continuationTokenService.tokenToString(notificationTemplatesPage.token!!) else null
            notification_templates = notificationTemplatesPage.entities.map {
                val notificationStats = notificationService.findNotificationStats(it.templateId!!)
                val notificationTemplateEntityEnriched =
                    NotificationTemplateEntityEnriched(it, notificationStats.read, notificationStats.total)

                conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
            }
        }
    }

    override fun sendNotification(templateId: String, partyIds: MutableList<String>) {
        log.info { "Send notification: templateId=$templateId; partyIds=$partyIds" }
        notificationSenderService.sendNotification(templateId, partyIds)
    }

    override fun sendNotificationAll(templateId: String) {
        log.info { "Send notification all: templateId=$templateId" }
        notificationSenderService.sendNotificationAll(templateId)
    }
}
