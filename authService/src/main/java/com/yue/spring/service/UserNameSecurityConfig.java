package com.yue.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
public class UserNameSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PwdDetailService pwdDetailService;

    @Override
    public void configure(HttpSecurity builder) {
        PasswordAuthenticationProvider smsCodeAuthenticationProvider = new PasswordAuthenticationProvider(pwdDetailService,passwordEncoder);
        builder.authenticationProvider(smsCodeAuthenticationProvider);
    }
}
