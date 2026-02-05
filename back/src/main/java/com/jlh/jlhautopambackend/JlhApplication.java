package com.jlh.jlhautopambackend;

import com.jlh.jlhautopambackend.config.GarageProperties;
import com.jlh.jlhautopambackend.config.RgpdProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({GarageProperties.class, RgpdProperties.class})
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class JlhApplication {

    public static void main(String[] args) {

        SpringApplication.run(JlhApplication.class, args);

    }

}
