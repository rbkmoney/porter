package com.rbkmoney.porter.service.model

import com.rbkmoney.porter.repository.entity.NotificationStatus

data class NotificationFilter(
    val templateId: String,
    val status: NotificationStatus? = null,
)

fun NotificationFilter.toKeyParams(): HashMap<String, String> {
    return HashMap<String, String>().apply {
        put("template_id", templateId)
        status?.let { put("status", status.name) }
    }
}
