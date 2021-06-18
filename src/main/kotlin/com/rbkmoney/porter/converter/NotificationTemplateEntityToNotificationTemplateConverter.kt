package com.rbkmoney.porter.converter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateDistributionDetails
import com.rbkmoney.notification.NotificationTemplateState
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import org.springframework.context.annotation.Lazy
import java.util.*

@Component
class NotificationTemplateEntityToNotificationTemplateConverter(
    @Lazy private val conversionService: ConversionService
) : NotificatorConverter<NotificationTemplateEntityEnriched, NotificationTemplate> {

    override fun convert(notificationTemplateEnrichedEntity: NotificationTemplateEntityEnriched): NotificationTemplate {
        return NotificationTemplate().apply {
            val notificationTemplateEntity = notificationTemplateEnrichedEntity.notificationTemplateEntity
            templateId = notificationTemplateEntity.templateId.toString()
            title = notificationTemplateEntity.title
            content = Base64.getEncoder().encodeToString(
                notificationTemplateEntity.content?.toByteArray(StandardCharsets.UTF_8)
            )
            createdAt = TypeUtil.temporalToString(notificationTemplateEntity.createdAt)
            updatedAt = notificationTemplateEntity.updatedAt?.let { TypeUtil.temporalToString(it) }
            state = conversionService.convert(notificationTemplateEntity.status, NotificationTemplateState::class.java)
            if (notificationTemplateEnrichedEntity.readCount != null && notificationTemplateEnrichedEntity.totalCount != null) {
                distributionDetails = NotificationTemplateDistributionDetails(
                    notificationTemplateEnrichedEntity.readCount,
                    notificationTemplateEnrichedEntity.totalCount
                )
            }
        }
    }
}
