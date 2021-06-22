package com.rbkmoney.porter.service.model

import com.rbkmoney.porter.repository.entity.NotificationStatus

data class NotificationFilter(
    val status: NotificationStatus? = null,
)

fun NotificationFilter.toKeyParams(): HashMap<String, String> {
    return HashMap<String, String>().apply {
        status?.let { put("status", status.name) }
    }
}
