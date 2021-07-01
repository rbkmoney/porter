package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository :
    CrudRepository<NotificationEntity, Long>,
    NotificationRepositoryCustom {

    fun findByNotificationTemplateEntity(notificationTemplateEntity: NotificationTemplateEntity): List<NotificationEntity>

    fun findByNotificationTemplateEntityTemplateId(templateId: String): List<NotificationEntity>

    @Query(
        value = """SELECT total_count.total, read_count.read FROM
                    (SELECT count(*) AS total FROM notify.notification WHERE template_id=:templateId) AS total_count,
                    (SELECT count(*) AS read FROM notify.notification
                        WHERE template_id=:templateId
                            AND status=CAST('read' AS notify.notification_status)) AS read_count
                """,
        nativeQuery = true
    )
    fun findNotificationCount(@Param("templateId") templateId: Long): TotalNotificationProjection
}
