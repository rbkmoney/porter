package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.openapi.notification.model.InlineObject2
import com.rbkmoney.porter.converter.NotificatorConverter
import com.rbkmoney.porter.repository.entity.NotificationStatus
import org.springframework.stereotype.Component

@Component
class MarkAllStatusEnumToNotificationStatusConverter :
    NotificatorConverter<InlineObject2.StatusEnum, com.rbkmoney.porter.repository.entity.NotificationStatus> {

    override fun convert(status: InlineObject2.StatusEnum): NotificationStatus {
        return when (status) {
            InlineObject2.StatusEnum.READ -> com.rbkmoney.porter.repository.entity.NotificationStatus.read
            InlineObject2.StatusEnum.UNREAD -> com.rbkmoney.porter.repository.entity.NotificationStatus.unread
        }
    }
}
