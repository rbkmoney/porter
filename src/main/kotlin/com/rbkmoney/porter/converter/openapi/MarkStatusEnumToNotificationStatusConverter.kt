package com.rbkmoney.porter.converter.openapi

import com.rbkmoney.openapi.notification.model.InlineObject1
import com.rbkmoney.porter.converter.NotificatorConverter
import com.rbkmoney.porter.repository.entity.NotificationStatus
import org.springframework.stereotype.Component

@Component
class MarkStatusEnumToNotificationStatusConverter :
    NotificatorConverter<InlineObject1.StatusEnum, com.rbkmoney.porter.repository.entity.NotificationStatus> {

    override fun convert(status: InlineObject1.StatusEnum): NotificationStatus {
        return when (status) {
            InlineObject1.StatusEnum.READ -> com.rbkmoney.porter.repository.entity.NotificationStatus.read
            InlineObject1.StatusEnum.UNREAD -> com.rbkmoney.porter.repository.entity.NotificationStatus.unread
        }
    }
}
