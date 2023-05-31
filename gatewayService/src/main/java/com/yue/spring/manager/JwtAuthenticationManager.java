package com.yue.spring.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 认证自定义
 */
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    TokenStore tokenStore;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        return Mono.justOrEmpty(authentication)
                .filter(a -> a instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                .map(BearerTokenAuthenticationToken::getToken)
                .flatMap((accessToken ->{
                    OAuth2AccessToken oAuth2AccessToken=this.tokenStore.readAccessToken(accessToken);
                    if(oAuth2AccessToken==null){
                        return Mono.error(new InvalidTokenException("无效的token!"));
                    }
                    else if(oAuth2AccessToken.isExpired()){
                        return Mono.error(new InvalidTokenException("token已过期！"));
                    }
                    OAuth2Authentication oAuth2Authentication=this.tokenStore.readAuthentication(accessToken);
                    if(oAuth2AccessToken==null){
                        return Mono.error(new InvalidTokenException("无效的token!"));
                    }
                    else return Mono.just(oAuth2Authentication);
                })).cast(Authentication.class);
    }
}
