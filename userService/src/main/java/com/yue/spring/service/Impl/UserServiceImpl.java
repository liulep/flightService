package com.yue.spring.service.Impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.Exception.YueException;
import com.yue.spring.handler.utils.RandomUtils;
import com.yue.spring.mapper.UserMapper;
import com.yue.spring.pojo.Relation;
import com.yue.spring.pojo.User;
import com.yue.spring.pojo.po.UserCode;
import com.yue.spring.service.UserService;
import com.yue.spring.handler.utils.OauthUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String USER_REGISTER_CODE="user_code:";

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public User updateUserInfo(UserCode user) {
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        Integer id = Integer.parseInt(OauthUtils.getCurrentUser().getUserId());
        User user_info = this.userMapper.selectById(id);
        if(ObjectUtils.isNotEmpty(user.getBirthday())){
            Date birthday = user.getBirthday();
            String format = new SimpleDateFormat("yyyyMMdd").format(birthday);
            String zodiac = DateUtil.getZodiac(Integer.parseInt(format.substring(4, 6))-1, Integer.parseInt(format.substring(6)));
            String chineseZodiac = DateUtil.getChineseZodiac(Integer.parseInt(format.substring(0, 4)));
            user_info.setBirthday(user.getBirthday());
            user_info.setConstellation(zodiac);
            user_info.setChineseZodiac(chineseZodiac);
        }
        user_info.setSex(ObjectUtils.isEmpty(user.getSex())?"":user.getSex().equals("1")?"男":"女");
        user_info.setNickName(StringUtils.isNotBlank(user.getNickName())?user.getNickName():user_info.getUserName());
        user_info.setEmail(StringUtils.isNotBlank(user.getNickName())?user.getEmail():user_info.getEmail());
        userMapper.updateById(user_info);
        return user_info;
    }

    @Override
    public Page<Relation> relationListByUserId(Integer userId, Integer currentPage, Integer pageSize) {
        Page<Relation> page=new Page<>(currentPage,pageSize);
        return userMapper.relationListByUserId(page,userId);
    }

    @Override
    @Async
    public void sendMail(String email) {
        MimeMessage mimeMessage=sender.createMimeMessage();
        String code = RandomUtils.getRandom();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
            message.setSubject("【用户注册】");
            message.setFrom("absolutecold@163.com");
            message.setTo(email);
            SpringTemplateLoader templateLoader=new SpringTemplateLoader(resourceLoader,"classpath:templates");
            Configuration configuration=new Configuration(Configuration.VERSION_2_3_0);
            configuration.setTemplateLoader(templateLoader);
            Template template=configuration.getTemplate("email.html");
            Map<String,String> map=new HashMap<String,String>(){{
                put("code",code);
            }};
            StringWriter out=new StringWriter();
            template.process(map,out);
            message.setText(out.toString(),true);
            sender.send(mimeMessage);
            redisTemplate.opsForValue().set(USER_REGISTER_CODE+email,code,60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new YueException(e.getMessage());
        }
    }
}
