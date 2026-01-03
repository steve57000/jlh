package com.jlh.jlhautopambackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "garage")
public class GarageProperties {
    private String name;
    private String address;
    private String organizerEmail; // optionnel
    private String timezone;       // optionnel
}
