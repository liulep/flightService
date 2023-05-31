package com.yue.spring.fegin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("authservice")
public interface AuthClient {

    @PostMapping("/oauth/logout")
    public String logout();
}
