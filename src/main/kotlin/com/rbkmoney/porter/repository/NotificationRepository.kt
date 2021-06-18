package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : CrudRepository<NotificationEntity, Long> {

    fun findByNotificationTemplateEntity(notificationTemplateEntity: NotificationTemplateEntity): List<NotificationEntity>
}
