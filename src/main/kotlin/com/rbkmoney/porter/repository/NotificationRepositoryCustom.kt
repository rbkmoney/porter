package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.pagination.ContinuationToken

interface NotificationRepositoryCustom {

    fun findNotifications(
        template: NotificationTemplateEntity,
        status: NotificationStatus?,
        limit: Int = 10,
    ): List<NotificationEntity>

    fun findNotifications(continuationToken: ContinuationToken, limit: Int = 10): List<NotificationEntity>
}
