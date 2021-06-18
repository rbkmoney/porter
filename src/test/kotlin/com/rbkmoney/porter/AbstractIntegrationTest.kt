package com.rbkmoney.porter

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [PorterApplication::class], initializers = [AbstractIntegrationTest.Initializer::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AbstractIntegrationTest {

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            postresql.start()
            TestPropertyValues.of(
                "spring.datasource.url=${postresql.jdbcUrl}",
                "spring.datasource.username=${postresql.username}",
                "spring.datasource.password=${postresql.password}"
            ).applyTo(applicationContext.environment)
        }
    }

    companion object {
        val postresql = PostgreSQLContainer<Nothing>("postgres:12").apply {
            withDatabaseName("porter")
            withUsername("root")
            withPassword("password")
        }
    }
}
