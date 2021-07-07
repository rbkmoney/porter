package com.rbkmoney.porter.handler

import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateCreateRequest
import com.rbkmoney.notification.NotificationTemplateModifyRequest
import com.rbkmoney.notification.base.InvalidRequest
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import com.rbkmoney.porter.repository.TotalNotificationProjection
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.NotificationSenderService
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.NotificationTemplateService
import com.rbkmoney.porter.service.PartyService
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.atMostOnce
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.core.convert.ConversionService
import java.util.Base64

@MockBeans(
    *[
        MockBean(NotificationSenderService::class),
        MockBean(ContinuationTokenService::class),
        MockBean(PartyService::class)
    ]
)
@SpringBootTest(classes = [NotificationServiceHandler::class])
class NotificationServiceHandlerTest {

    @MockBean
    lateinit var conversionService: ConversionService

    @MockBean
    lateinit var notificationService: NotificationService

    @MockBean
    lateinit var notificationTemplateService: NotificationTemplateService

    @Autowired
    lateinit var notificationServiceHandler: NotificationServiceHandler

    @Test
    fun `test wrong content type on create notification template`() {
        val request = NotificationTemplateCreateRequest("testTitle", "<p>I really like using Markdown.</p>")
        assertThrows<InvalidRequest> {
            notificationServiceHandler.createNotificationTemplate(request)
        }
    }

    @Test
    fun `create notification template test`() {
        // Given
        val title = "testTitle"
        val content = "<p>I really like using Markdown.</p>"
        val createRequest =
            NotificationTemplateCreateRequest(title, Base64.getEncoder().encodeToString(content.toByteArray()))

        // When
        `when`(notificationTemplateService.createNotificationTemplate(anyString(), anyString())).thenReturn(
            NotificationTemplateEntity()
        )
        `when`(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())

        val createNotificationTemplate = notificationServiceHandler.createNotificationTemplate(createRequest)

        // Then
        verify(notificationTemplateService, atMostOnce()).createNotificationTemplate(anyString(), anyString())
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }

    @Test
    fun `modify notification template test`() {
        // Given
        val templateId = "testTemplateId"
        val templateTitle = "testTemplateTitle"
        val templateContent = "<p>I really like using Markdown.</p>"

        // When
        whenever(
            notificationTemplateService.editNotificationTemplate(
                eq(templateId),
                eq(templateTitle),
                eq(templateContent),
                anyOrNull()
            )
        ).thenReturn(NotificationTemplateEntity().apply { this.templateId = templateId })
        whenever(notificationService.findNotificationStats(eq(templateId))).thenReturn(
            object : TotalNotificationProjection {
                override val total: Long
                    get() = 10
                override val read: Long
                    get() = 10
            }
        )
        whenever(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())
        notificationServiceHandler.modifyNotificationTemplate(
            NotificationTemplateModifyRequest(templateId).apply {
                title = templateTitle
                content = templateContent
            }
        )

        // Then
        verify(notificationTemplateService, atMostOnce()).editNotificationTemplate(
            eq(templateId),
            eq(templateTitle),
            eq(templateContent),
            anyOrNull()
        )
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }

    @Test
    fun `get notification template test`() {
        // Given
        val templateId = "testTemplateId"

        // When
        whenever(notificationTemplateService.getNotificationTemplate(eq(templateId)))
            .thenReturn(NotificationTemplateEntity().apply { this.templateId = templateId })
        whenever(notificationService.findNotificationStats(eq(templateId))).thenReturn(
            object : TotalNotificationProjection {
                override val total: Long
                    get() = 10
                override val read: Long
                    get() = 10
            }
        )
        whenever(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())
        notificationServiceHandler.getNotificationTemplate(templateId)

        // Then
        verify(notificationTemplateService, atMostOnce()).getNotificationTemplate(eq(templateId))
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }
}
