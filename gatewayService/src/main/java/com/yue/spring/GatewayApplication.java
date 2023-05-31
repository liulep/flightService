package com.yue.spring;

import com.yue.gray.rule.VersionServiceInstanceListSupplierConfiguration;
import com.yue.spring.config.DynamicRoutingConfigForApp;
import com.yue.spring.config.SysParameterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(value = {SysParameterConfig.class})
@LoadBalancerClients(value = {
        @LoadBalancerClient(value = "queryservice",configuration = VersionServiceInstanceListSupplierConfiguration.class)
})
public class GatewayApplication implements CommandLineRunner {

    @Autowired
    private DynamicRoutingConfigForApp dynamicRoutingConfigForApp;


    public static void main(String[] args) {
        System.setProperty("csp.sentinel.app.type","1");
        SpringApplication.run(GatewayApplication.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        dynamicRoutingConfigForApp.refreshRouting();
    }
}
