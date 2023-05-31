package com.yue.spring.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.yue.spring.pojo.Filter;
import com.yue.spring.pojo.Predicate;
import com.yue.spring.pojo.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;


@Component
@Slf4j
@RefreshScope
public class DynamicRoutingConfig implements ApplicationEventPublisherAware{

    @Value("${dynamic.route.server-addr}")
    private String serverAddr;

    @Value("${dynamic.route.namespace}")
    private String namespace;

    @Value("${dynamic.route.data-id}")
    private String dataId;

    @Value("${dynamic.route.group}")
    private String groupId;

    // 保存、删除路由服务
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    public ConfigService getNacosConfigInfo() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        ConfigService configService = NacosFactory.createConfigService(properties);
        return configService;
    }


    @PostConstruct
    public void initRouting() {
        try {
            ConfigService configService = this.getNacosConfigInfo();
            String configInfo = configService.getConfig(dataId, groupId, 5000);
            if (null != configInfo) {
                List<Route> list = JSONObject.parseArray(configInfo).toJavaList(Route.class);
                for (Route route : list) {
                    update(assembleRouteDefinition(route));
                }
            }
            refreshRouting();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }




    public void refreshRouting() {
        try {
            ConfigService configService = this.getNacosConfigInfo();
            //监听路由变化
            configService.addListener(
                    dataId,
                    groupId,
                    new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return null;
                        }
                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            try {
                                if (null != configInfo) {
                                    List<Route> list = JSONObject.parseArray(configInfo).toJavaList(Route.class);
                                    //更新路由表
                                    for (Route route : list) {
                                        update(assembleRouteDefinition(route));
                                    }
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }


    private void update(RouteDefinition routeDefinition) throws Exception {
        //先删除路由
        routeDefinitionWriter.delete(Mono.just(routeDefinition.getId()));
        //再保存路由
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        //发布事件刷新路由
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }


    private RouteDefinition assembleRouteDefinition(Route routeEntity) {
        RouteDefinition definition = new RouteDefinition();
        // ID
        definition.setId(routeEntity.getId());

        // Predicates断言
        List<PredicateDefinition> pdList = new ArrayList<>();
        for (Predicate predicateEntity : routeEntity.getPredicates()) {
            PredicateDefinition predicateDefinition = new PredicateDefinition();
            predicateDefinition.setArgs(predicateEntity.getArgs());
            predicateDefinition.setName(predicateEntity.getName());
            pdList.add(predicateDefinition);
        }
        definition.setPredicates(pdList);

        // Filters过滤器
        List<FilterDefinition> fdList = new ArrayList<>();
        for (Filter filterEntity : routeEntity.getFilters()) {
            FilterDefinition filterDefinition = new FilterDefinition();
            filterDefinition.setArgs(filterEntity.getArgs());
            filterDefinition.setName(filterEntity.getName());
            fdList.add(filterDefinition);
        }
        definition.setFilters(fdList);

        // URI
        URI uri = UriComponentsBuilder.fromUriString(routeEntity.getUri()).build().toUri();
        definition.setUri(uri);

        return definition;
    }
}
