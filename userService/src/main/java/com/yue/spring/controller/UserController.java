package com.yue.spring.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yue.spring.Exception.YueException;
import com.yue.spring.fegin.AuthClient;
import com.yue.spring.group.GroupInster;
import com.yue.spring.group.GroupUpdate;
import com.yue.spring.group.NotControllerResponseAdvice;
import com.yue.spring.handler.utils.FileUtil;
import com.yue.spring.mapper.UserMapper;
import com.yue.spring.pojo.Relation;
import com.yue.spring.pojo.User;
import com.yue.spring.pojo.po.UserCode;
import com.yue.spring.service.RelationService;
import com.yue.spring.service.UserService;
import com.yue.spring.handler.utils.OauthUtils;
import com.yue.spring.handler.utils.RandomUtils;
import com.yue.spring.handler.utils.ValidatorUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController implements ApplicationContextAware {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RelationService relationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AuthClient authClient;

    private final static String USER_REGISTER_CODE="user_code:";

    private ApplicationContext context;

    //用户注册
    @PostMapping("/register")
    public String register(@RequestBody @Validated(GroupInster.class) UserCode user){
        User userRes = userMapper.selectOne(new QueryWrapper<User>().eq("user_name", user.getUserName()));
        String code = redisTemplate.opsForValue().get(USER_REGISTER_CODE + user.getEmail());
        if(ObjectUtils.isNotEmpty(userRes))
            throw new YueException("该用户名已被注册");
        if (code==null)
            throw new YueException("请先获取验证码");
        else if(!code.equals(user.getCode()))
            throw new YueException("验证码不正确");
        user.setNickName(user.getUserName());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userMapper.insert(user);
        redisTemplate.opsForValue().set(USER_REGISTER_CODE+user.getEmail(),"",1,TimeUnit.SECONDS);
        return "注册成功";
    }

    //获取验证码
    @GetMapping("/sendCode/{email}")
    public String getCode(@PathVariable("email") String email){
        ValidatorUtils.isEmail(email);
        if(redisTemplate.opsForValue().get(USER_REGISTER_CODE+email)!=null){
            return "请勿重复发送验证码！";
        }
        userService.sendMail(email);
        return "验证码已发送！60秒之内有效";
    }


    //修改用户信息
    @PostMapping("/update")
    public User updateUserInfo(@RequestBody @Validated(GroupUpdate.class) UserCode user){
        return userService.updateUserInfo(user);
    }

    //获取用户信息
    @GetMapping("/user_info")
    public User getUserInfo(){
        return userService.getById(OauthUtils.getCurrentUser().getUserId());
    }

    //根据某个用户Id获取信息
    @GetMapping("/userInfo/getById/{id}")
    @NotControllerResponseAdvice
    public User getUserInfo(@PathVariable("id")Integer id){
        return userService.getById(id);
    }

    //修改用户头像
    @PostMapping("/update/userImg")
    @NotControllerResponseAdvice
    public User updateUserImg(@RequestBody User user){
        userService.updateById(user);
        return userService.getById(user.getId());
    }

    //上传用户头像
    @PostMapping("/upload")
    public User upload(@RequestParam("file") MultipartFile file) throws IOException {
        String s = FileUtil.fileUpload(file);
        User user = userService.getById(OauthUtils.getCurrentUser().getUserId());
        user.setImage(s);
        userService.updateById(user);
        return userService.getById(OauthUtils.getCurrentUser().getUserId());
    }

    //修改密码
    @PostMapping("/update_password")
    public String updatePassword(@RequestBody Map<String,String> map){
        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");
        if(StringUtils.isBlank(oldPassword)||StringUtils.isBlank(newPassword)){
            throw new YueException("旧密码或者新密码不能为空");
        }
        User user=userService.getById(OauthUtils.getCurrentUser().getUserId());
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        if(!encoder.matches(oldPassword,user.getPassword())){
            throw new YueException("旧密码不正确！");
        }
        user.setPassword(encoder.encode(newPassword));
        userService.updateById(user);
        authClient.logout();
        return "密码修改成功!请重新登录";
    }

    //获取用户联系人列表
    @GetMapping("/relation/user")
    public Page<Relation> getRelationList(@RequestParam(value = "currentPage",defaultValue = "1")Integer currentPage,
                                          @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        return userService.relationListByUserId(Integer.parseInt(OauthUtils.getCurrentUser().getUserId()),currentPage,pageSize);
    }

    //增加/修改联系人
    @PostMapping("/relation/save")
    public String relationSave(@RequestBody @Valid Relation relation){
        relation.setUserId(Integer.parseInt(OauthUtils.getCurrentUser().getUserId()));
        relationService.saveOrUpdate(relation);
        return "操作成功";
    }

    //删除联系人
    @PostMapping("/relation/delete")
    public String relationDel(@RequestBody Integer [] ids){
        relationService.removeBatchByIds(Arrays.asList(ids));
        return "删除成功";
    }

    //获取联系人信息
    @GetMapping("/user_relation/{id}")
    @NotControllerResponseAdvice
    public Relation getRelationById(@PathVariable("id")Integer id){
        Relation byId = relationService.getById(id);
        log.error("/user_relation :{}",byId);
        return byId;
    }

    //健康检测
    @GetMapping("/health")
    public String health(){
        return "success";
    }

    //服务停止
    @PostMapping("/shutdown")
    public void shutdown(){
        ConfigurableApplicationContext run = (ConfigurableApplicationContext) context;
        run.close();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context=applicationContext;
    }
}
