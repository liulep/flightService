package com.yue.spring.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface PwdDetailService {

    UserDetails loadUserByMobile(String username) throws UsernameNotFoundException;
}
