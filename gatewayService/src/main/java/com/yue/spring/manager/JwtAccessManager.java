package com.yue.spring.manager;

import cn.hutool.core.convert.Convert;
import com.google.common.collect.Lists;
import com.yue.spring.constant.SysConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * 鉴权自定义
 */
@Component
@Slf4j
@Deprecated
public class JwtAccessManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    //预留 多权限时
    @PostConstruct
    public void init(){
        redisTemplate.opsForHash().put(SysConstant.OAUTH_URLS,"/query/login/info", Lists.newArrayList("ROLE_user"));
        redisTemplate.opsForHash().put(SysConstant.OAUTH_URLS,"/query/login/admin", Lists.newArrayList("ROLE_user"));
        redisTemplate.opsForHash().put(SysConstant.OAUTH_URLS,"/order/info", Lists.newArrayList("ROLE_user","user"));
        redisTemplate.opsForHash().put(SysConstant.OAUTH_URLS,"/order/listByUserId", Lists.newArrayList("ROLE_user"));
        redisTemplate.opsForHash().put(SysConstant.OAUTH_URLS,"/oauth/logout", Lists.newArrayList("ROLE_user"));
        redisTemplate.opsForHash().put(SysConstant.OAUTH_URLS,"/user/register", Lists.newArrayList("ROLE_user"));
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
//        URI url=object.getExchange().getRequest().getURI();
//        Object value=redisTemplate.opsForHash().get(SysConstant.OAUTH_URLS,url.getPath());
//        List<String> authorities= Convert.toList(String.class,value);
        //不判断权限直接通过
        return Mono.just(new AuthorizationDecision(true));
    }
}
