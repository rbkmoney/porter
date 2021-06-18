package com.rbkmoney.porter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.NotificationTemplateState
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import com.rbkmoney.porter.service.NotificationService
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.util.Base64
import java.util.UUID

@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate"
    ]
)
class NotificationServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var notificationTemplateRepository: NotificationTemplateRepository

    @Autowired
    lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        notificationRepository.deleteAll()
        notificationTemplateRepository.deleteAll()
    }

    @Test
    fun `create notification test`() {
        // Given
        val title = "test title"
        val content = "<p>I really like using Markdown.</p>"

        // When
        val notificationTemplate = notificationService.createNotificationTemplate(title, content)

        // Then
        assertNotNull(notificationTemplate.templateId)
        assertEquals(title, notificationTemplate.title)
        assertEquals(content, String(Base64.getDecoder().decode(notificationTemplate.content)))
        assertEquals(NotificationTemplateState.draft_state, notificationTemplate.state)
        assertNull(notificationTemplate.distributionDetails)
    }

    @Test
    @Transactional
    fun `modify notification test`() {
        // Given
        val templateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            content = Base64.getEncoder().encodeToString("<p>I really like using Markdown.</p>".toByteArray())
            templateId = UUID.randomUUID().toString()
        }

        // When
        val savedTemplateEntity = notificationTemplateRepository.save(templateEntity)
        val title = "test title"
        val content = "**bold text**"
        val editedNotificationTemplate = notificationService.editNotificationTemplate(
            savedTemplateEntity.templateId!!,
            title,
            content
        )

        // Then
        assertEquals(savedTemplateEntity.title, editedNotificationTemplate.title)
        assertNotEquals(savedTemplateEntity.content, editedNotificationTemplate.content)
        assertEquals(content, String(Base64.getDecoder().decode(editedNotificationTemplate.content)))
    }

    @Test
    fun `get notification test`() {
        // Given
        val templateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            content = "<p>I really like using Markdown.</p>"
            status = NotificationTemplateStatus.draft
        }

        // When
        notificationTemplateRepository.save(templateEntity)
        val notificationTemplate = notificationService.getNotificationTemplate(templateEntity.templateId!!)

        // Then
        assertEquals(templateEntity.templateId, notificationTemplate.templateId)
        assertEquals(
            templateEntity.createdAt.withNano(0),
            TypeUtil.stringToLocalDateTime(notificationTemplate.createdAt).withNano(0)
        )
        assertEquals(
            templateEntity.updatedAt?.withNano(0),
            TypeUtil.stringToLocalDateTime(notificationTemplate.updatedAt).withNano(0)
        )
        assertEquals(templateEntity.title, notificationTemplate.title)
        assertEquals(templateEntity.content, String(Base64.getDecoder().decode(notificationTemplate.content)))
        assertTrue(notificationTemplate.state == NotificationTemplateState.draft_state)
    }
}
