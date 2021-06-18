package com.rbkmoney.porter.converter

import com.rbkmoney.notification.NotificationStatus
import com.rbkmoney.notification.Party
import com.rbkmoney.notification.PartyNotification
import com.rbkmoney.porter.converter.model.NotificationEntityEnriched
import org.springframework.context.annotation.Lazy
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component

@Component
class NotificationEntityToPartyNotificationConverter(
    @Lazy private val conversionService: ConversionService,
) : NotificatorConverter<NotificationEntityEnriched, PartyNotification> {

    override fun convert(notificationEntityEnriched: NotificationEntityEnriched): PartyNotification {
        return PartyNotification().apply {
            val notificationEntity = notificationEntityEnriched.notificationEntity
            templateId = notificationEntity.notificationTemplateEntity?.templateId
            party = Party(notificationEntityEnriched.notificationEntity.partyId!!, notificationEntityEnriched.partyName)
            status = conversionService.convert(notificationEntity.status, NotificationStatus::class.java)
        }
    }
}
