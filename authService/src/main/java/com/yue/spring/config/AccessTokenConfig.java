package com.yue.spring.config;

import com.yue.spring.pojo.SecurityUser;
import com.yue.spring.pojo.constant.TokenConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.LinkedHashMap;

/**
 * 令牌的配置
 */
@Configuration
public class AccessTokenConfig {
    /**
     * 令牌的存储策略
     */
    @Bean
    public TokenStore tokenStore() {
        //使用JwtTokenStore生成JWT令牌
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * 在JWT编码的令牌值和OAuth身份验证信息之间进行转换。
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter converter = new JwtAccessTokenEnhancer();
        converter.setSigningKey(TokenConstant.SIGN_KEY);
        converter.setAccessTokenConverter(new JwtEnhanceAccessTokenConverter());
        return converter;
    }


    public static class JwtAccessTokenEnhancer extends JwtAccessTokenConverter {
        /**
         * 重写enhance方法
         */
        @Override
        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
            Object principal = authentication.getUserAuthentication().getPrincipal();
            if (principal instanceof SecurityUser){
                //获取userDetailService中查询到用户信息
                SecurityUser user=(SecurityUser)principal;
                //将额外的信息放入到LinkedHashMap中
                LinkedHashMap<String,Object> extendInformation=new LinkedHashMap<>();
                //设置用户的userId
                extendInformation.put(TokenConstant.USER_ID,user.getUserId());
                //添加到additionalInformation
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(extendInformation);
            }
            return super.enhance(accessToken, authentication);
        }
    }
}
