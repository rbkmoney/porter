package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : CrudRepository<NotificationEntity, Long>, NotificationRepositoryCustom {

    fun findByNotificationTemplateEntity(notificationTemplateEntity: NotificationTemplateEntity): List<NotificationEntity>

    fun findByNotificationTemplateEntityTemplateId(templateId: String): List<NotificationEntity>

    @Query(
        value = """SELECT CAST(tr.cnt AS bigint) AS total, CAST(rr.cnt2 AS bigint) AS read FROM
                    (SELECT count(*) AS cnt FROM notify.notification ne WHERE ne.template_id=:templateId) AS tr,
                    (SELECT count(*) AS cnt2 FROM notify.notification ne WHERE ne.template_id=:templateId AND ne.status=CAST('read' AS notify.notification_status)) AS rr
                """,
        nativeQuery = true
    )
    fun findNotificationCount(@Param("templateId") templateId: Long): TotalNotificationProjection
}
