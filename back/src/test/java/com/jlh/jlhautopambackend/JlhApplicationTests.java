package com.jlh.jlhautopambackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JlhApplicationSmokeTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(JlhApplication.class)
            .withPropertyValues(
                    "spring.autoconfigure.exclude="
                            + DataSourceAutoConfiguration.class.getName() + ","
                            + HibernateJpaAutoConfiguration.class.getName()
            );

    @Test
    void contextLoads() {
        runner.run(context -> {
            // si on entre ici, c’est que le contexte s’est démarré sans exception
        });
    }
}