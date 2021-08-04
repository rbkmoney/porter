package com.rbkmoney.porter.controller

import com.rbkmoney.openapi.notification.api.NotificationApi
import com.rbkmoney.openapi.notification.model.InlineObject
import com.rbkmoney.openapi.notification.model.InlineObject1
import com.rbkmoney.openapi.notification.model.InlineObject2
import com.rbkmoney.openapi.notification.model.Notification
import com.rbkmoney.openapi.notification.model.NotificationSearchResult
import com.rbkmoney.openapi.notification.model.NotificationStatus
import com.rbkmoney.porter.service.KeycloakService
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import mu.KotlinLogging
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

private val log = KotlinLogging.logger {}

@RestController
class NotificationController(
    private val keycloakService: KeycloakService,
    private val notificationService: NotificationService,
    private val conversionService: ConversionService,
    private val continuationTokenService: ContinuationTokenService,
) : NotificationApi {

    override fun deleteNotification(xRequestID: String, id: UUID): ResponseEntity<Void> {
        log.info { "Delete notification. requestId=$xRequestID; notificationsId=$id" }
        val partyId = keycloakService.partyId
        notificationService.softDeleteNotification(partyId, id.toString())
        return ResponseEntity.ok().build()
    }

    override fun deleteNotifications(xRequestID: String, request: InlineObject): ResponseEntity<Void> {
        log.info { "Delete notifications. requestId=$xRequestID; notificationIds=${request.notificationIds}" }
        val partyId = keycloakService.partyId
        notificationService.softDeleteNotification(
            partyId,
            *request.notificationIds.map { it.toString() }.toTypedArray()
        )
        return ResponseEntity.ok().build()
    }

    override fun notification(xRequestID: String, id: UUID): ResponseEntity<Notification> {
        log.info { "Get notification. requestId=$xRequestID; notificationId=$id" }
        val notification = notificationService.getNotification(id.toString())
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(conversionService.convert(notification, Notification::class.java)!!)
    }

    override fun notificationList(
        xRequestID: String?,
        title: String?,
        status: NotificationStatus?,
        fromTime: OffsetDateTime?,
        toTime: OffsetDateTime?,
        limit: Int?,
        continuationToken: String?,
    ): ResponseEntity<NotificationSearchResult> {
        log.info {
            """Find notifications. requestId=$xRequestID; title=$title; status=$status; fromTime=$fromTime
               toTime=$toTime, limit=$limit; continuationToken=$continuationToken"""
        }
        val partyId = keycloakService.partyId
        val notificationStatus =
            conversionService.convert(status, com.rbkmoney.porter.repository.entity.NotificationStatus::class.java)
        val notificationPage = notificationService.findNotifications(
            filter = NotificationFilter(
                partyId = partyId,
                status = notificationStatus,
                fromTime = fromTime?.toLocalDateTime(),
                toTime = toTime?.toLocalDateTime(),
                title = title,
                deleted = false
            ),
            continuationToken = continuationToken?.let { continuationTokenService.tokenFromString(continuationToken) },
            limit = limit ?: 10
        )
        val notifications = notificationPage.entities.map { conversionService.convert(it, Notification::class.java) }
        val searchResult = NotificationSearchResult().apply {
            result = notifications
            this.continuationToken = notificationPage.token?.let {
                continuationTokenService.tokenToString(it)
            }
        }
        return ResponseEntity.ok(searchResult)
    }

    override fun notificationMark(xRequestID: String, request: InlineObject1): ResponseEntity<Void> {
        log.info {
            "Mark notification. requestId=$xRequestID; " +
                "status=${request.status} notificationIds=${request.notificationIds}"
        }
        val notificationStatus = conversionService.convert(
            request.status,
            com.rbkmoney.porter.repository.entity.NotificationStatus::class.java
        )!!
        val partyId = keycloakService.partyId
        notificationService.notificationMark(partyId, request.notificationIds.map { it.toString() }, notificationStatus)
        return ResponseEntity.ok().build()
    }

    override fun notificationMarkAll(xRequestID: String?, request: InlineObject2): ResponseEntity<Void> {
        log.info { "Mark all notification. requestId=$xRequestID, status=${request.status}" }
        val notificationStatus = conversionService.convert(
            request.status,
            com.rbkmoney.porter.repository.entity.NotificationStatus::class.java
        )!!
        val partyId = keycloakService.partyId
        notificationService.notificationMarkAll(partyId, notificationStatus)

        return ResponseEntity.ok().build()
    }
}
