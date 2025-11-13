package com.jlh.jlhautopambackend;

import com.jlh.jlhautopambackend.config.GarageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(GarageProperties.class)
public class JlhApplication {

    public static void main(String[] args) {

        SpringApplication.run(JlhApplication.class, args);

    }

}
