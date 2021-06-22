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
            postgresql.start()
            TestPropertyValues.of(
                "spring.datasource.url=${postgresql.jdbcUrl}",
                "spring.datasource.username=${postgresql.username}",
                "spring.datasource.password=${postgresql.password}",
                "spring.jpa.show_sql=true"
            ).applyTo(applicationContext.environment)
        }
    }

    companion object {
        val postgresql = PostgreSQLContainer<Nothing>("postgres:12").apply {
            withDatabaseName("porter")
            withUsername("root")
            withPassword("password")
        }
    }
}
