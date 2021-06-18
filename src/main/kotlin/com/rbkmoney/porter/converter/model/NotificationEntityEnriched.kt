package com.rbkmoney.porter.converter.model

import com.rbkmoney.porter.repository.entity.NotificationEntity

data class NotificationEntityEnriched(
    val notificationEntity: NotificationEntity,
    val partyId: String,
    val partyName: String,
)
