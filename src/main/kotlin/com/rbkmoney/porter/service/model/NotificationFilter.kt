package com.rbkmoney.porter.service.model

import com.rbkmoney.porter.repository.entity.NotificationStatus

data class NotificationFilter(
    val templateId: String,
    val status: NotificationStatus? = null,
)
