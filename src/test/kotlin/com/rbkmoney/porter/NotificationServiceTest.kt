package com.rbkmoney.porter

import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.model.NotificationFilter
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.util.UUID
import java.util.stream.Collectors
import java.util.stream.Stream

@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate"
    ]
)
class NotificationServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var notificationTemplateRepository: NotificationTemplateRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var partyRepository: PartyRepository

    @Autowired
    lateinit var notificationService: NotificationService

    lateinit var notificationTemplateEntity: NotificationTemplateEntity

    @BeforeEach
    internal fun setUp() {
        notificationRepository.deleteAll()
        notificationTemplateRepository.deleteAll()
        partyRepository.deleteAll()
        notificationTemplateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            templateId = TEMPLATE_ID
        }
        notificationTemplateRepository.save(notificationTemplateEntity)
    }

    @Test
    fun `find notification pagination test`() {
        // Given
        val partyEntities = EasyRandom().objects(PartyEntity::class.java, 10)
            .peek { it.partyStatus = PartyStatus.active }
            .collect(Collectors.toList())

        // When
        partyRepository.saveAll(partyEntities)
        notificationService.createNotifications(TEMPLATE_ID)
        val page = notificationService.findNotifications(NotificationFilter(TEMPLATE_ID), limit = 5)
        val secondPage = notificationService.findNotifications(NotificationFilter(TEMPLATE_ID), continuationToken = page.token, limit = 10)

        // Then
        assertTrue(page.hasNext)
        assertTrue(page.entities.size == 5)
        assertFalse(secondPage.hasNext)
        assertTrue(secondPage.entities.size == 5)
    }

    @Test
    fun `find notification pagination with status param`() {
        // Given
        val notificationEntities = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
            }
            .collect(Collectors.toList())

        // When
        notificationRepository.saveAll(notificationEntities)
        val page = notificationService.findNotifications(
            filter = NotificationFilter(TEMPLATE_ID, status = NotificationStatus.unread),
            limit = 20
        )

        // Then
        assertFalse(page.hasNext)
        assertTrue(page.entities.size == 10)
    }

    @Test
    fun `find total notification`() {
        // Given
        val unreadNotificationsStream = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = UUID.randomUUID().toString()
                it.status = NotificationStatus.unread
            }
        val readNotificationStream = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = UUID.randomUUID().toString()
                it.status = NotificationStatus.read
            }
        val notifications =
            Stream.concat(unreadNotificationsStream, readNotificationStream).collect(Collectors.toList())

        // When
        notificationRepository.saveAll(notifications)
        val notificationTotal = notificationService.findNotificationTotal(TEMPLATE_ID)

        // Then
        assertTrue(notificationTotal.total == 20L)
        assertTrue(notificationTotal.read == 10L)
    }

    companion object {
        const val TEMPLATE_ID = "testTemplateId"
    }
}
