package com.rbkmoney.porter.extensions

import com.rbkmoney.porter.repository.entity.NotificationStatus

fun com.rbkmoney.notification.NotificationStatus.toEntityStatus(): NotificationStatus {
    return when (this) {
        com.rbkmoney.notification.NotificationStatus.read -> NotificationStatus.read
        com.rbkmoney.notification.NotificationStatus.unread -> NotificationStatus.unread
    }
}
