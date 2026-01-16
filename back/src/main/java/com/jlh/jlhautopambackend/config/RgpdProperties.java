package com.jlh.jlhautopambackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rgpd")
public class RgpdProperties {
    private boolean anonymizationEnabled = true;
    private long retentionDays = 1095;
    private String anonymizationCron = "0 0 3 * * *";
}
