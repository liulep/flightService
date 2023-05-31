package com.yue.spring.service;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;


public class PasswordAuthenticationProvider implements AuthenticationProvider {

    private final PwdDetailService userDetailService;

    private final PasswordEncoder passwordEncoder;


    public PasswordAuthenticationProvider(PwdDetailService userDetailService, PasswordEncoder passwordEncoder){
        this.userDetailService=userDetailService;
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        PasswordAuthenticationToken authenticationToken = (PasswordAuthenticationToken) authentication;
        String username = (String) authenticationToken.getPrincipal();
        String password = (String) authenticationToken.getCredentials();
        //查询数据库，加载用户详细信息
        UserDetails user = userDetailService.loadUserByMobile(username);
        if (user == null) {
            throw new InternalAuthenticationServiceException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        PasswordAuthenticationToken authenticationResult = new PasswordAuthenticationToken(user, password,user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
