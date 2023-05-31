package com.yue.spring.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth2.cloud.sys.parameter")
@Data
public class SysParameterConfig {
    /**
     * 白名单
     */
    private List<String> ignoreUrls;
}
