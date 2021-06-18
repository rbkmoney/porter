package com.rbkmoney.porter.converter

import com.rbkmoney.notification.NotificationTemplateState
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import org.springframework.stereotype.Component

@Component
class NotificationTemplateStatusToNotificationTemplateStateConverter :
    NotifyConverter<NotificationTemplateStatus, NotificationTemplateState> {

    override fun convert(status: NotificationTemplateStatus): NotificationTemplateState? {
        return when (status) {
            NotificationTemplateStatus.final -> NotificationTemplateState.final_state
            NotificationTemplateStatus.draft -> NotificationTemplateState.draft_state
            else -> throw IllegalArgumentException("Unknown notification template status: ${status.name}")
        }
    }
}
