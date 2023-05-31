package com.yue.spring.fliter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yue.gray.rule.GrayConstant;
import com.yue.gray.utils.GrayRequestContextHolder;
import com.yue.spring.config.SysParameterConfig;
import com.yue.spring.constant.SysConstant;
import com.yue.spring.constant.TokenConstant;
import com.yue.spring.pojo.R;
import com.yue.spring.pojo.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 全局过滤器
 */
@Component
@Slf4j
public class GlobalAuthenticationFilter implements GlobalFilter, Ordered {
    /**
     * JWT令牌的服务
     */
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 系统参数配置
     */
    @Autowired
    private SysParameterConfig sysConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUrl = exchange.getRequest().getPath().value();

        //灰度发布拦截
        interceptGray(exchange);

        //白名单放行
        if (checkUrls(sysConfig.getIgnoreUrls(),requestUrl)){
            return chain.filter(exchange);
        }

        //检查token是否存在
        String token = getToken(exchange);
        if (StringUtils.isBlank(token)) {
            return invalidTokenMono(exchange);
        }

        //判断是否是有效的token
        OAuth2AccessToken oAuth2AccessToken;
        try {
            //解析token
            oAuth2AccessToken = tokenStore.readAccessToken(token);
            Map<String, Object> additionalInformation = oAuth2AccessToken.getAdditionalInformation();
            //令牌的唯一ID
            String jti=additionalInformation.get(TokenConstant.JTI).toString();
            Boolean hasKey = stringRedisTemplate.hasKey(SysConstant.JTI_KEY_PREFIX + jti);
            if (Boolean.TRUE.equals(hasKey))
                return invalidTokenMono(exchange);
            //取出用户身份信息
            String user_name = additionalInformation.get("user_name").toString();
            //获取用户权限
            List<String> authorities = (List<String>) additionalInformation.get("authorities");
            //从additionalInformation取出userId
            String userId = additionalInformation.get(TokenConstant.USER_ID).toString();
            JSONObject jsonObject=new JSONObject();
            jsonObject.put(TokenConstant.PRINCIPAL_NAME, user_name);
            //jsonObject.put(TokenConstant.USER_INFO,additionalInformation.get(TokenConstant.USER_INFO));
            jsonObject.put(TokenConstant.AUTHORITIES_NAME,authorities);
            //过期时间，单位秒
            jsonObject.put(TokenConstant.EXPR,oAuth2AccessToken.getExpiresIn());
            jsonObject.put(TokenConstant.JTI,jti);
            //封装到JSON数据中
            jsonObject.put(TokenConstant.USER_ID, userId);
            //将解析后的token加密放入请求头中
            String base64 = Base64.encode(jsonObject.toJSONString());
            //放入请求头中
            ServerHttpRequest tokenRequest = exchange.getRequest().mutate()
                    //将令牌传递过去
                    .header(TokenConstant.TOKEN_NAME, base64)
                    //将灰度标记传递过去
                    .header(GrayConstant.GRAY_HEADER,GrayRequestContextHolder.getGrayTag().toString())
                    .build();
            ServerWebExchange build = exchange.mutate().request(tokenRequest).build();
            return chain.filter(build);
        } catch (InvalidTokenException e) {
            //解析token异常，直接返回token无效
            return invalidTokenMono(exchange);
        }


    }

    /**
     * 灰度发布的拦截标记
     */
    private void interceptGray(ServerWebExchange exchange){
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if (headers.containsKey(GrayConstant.GRAY_HEADER)){
            String gray = headers.getFirst(GrayConstant.GRAY_HEADER);
            if (StrUtil.isNotBlank(gray)){
                //设置灰度标记
                GrayRequestContextHolder.setGrayTag(gray);
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 对url进行校验匹配
     */
    private boolean checkUrls(List<String> urls,String path){
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String url : urls) {
            if (pathMatcher.match(url,path))
                return true;
        }
        return false;
    }

    /**
     * 从请求头中获取Token
     */
    private String getToken(ServerWebExchange exchange) {
        String tokenStr = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isBlank(tokenStr)) {
            return null;
        }
        String token = tokenStr.split(" ")[1];
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return token;
    }

    /**
     * 无效的token
     */
    private Mono<Void> invalidTokenMono(ServerWebExchange exchange) {
        return buildReturnMono(R.setResult(ResultCodeEnum.INVALID_TOKEN), exchange);
    }


    private Mono<Void> buildReturnMono(R resultMsg, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        byte[] bits = JSON.toJSONString(resultMsg).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset:utf-8");
        return response.writeWith(Mono.just(buffer));
    }
}
