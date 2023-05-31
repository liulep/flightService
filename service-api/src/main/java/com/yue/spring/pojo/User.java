package com.yue.spring.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yue.spring.group.GroupInster;
import com.yue.spring.group.GroupUpdate;
import lombok.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.lang.annotation.Target;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

    private static final long serialVersionUID=986823857621547280L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "用户名不能为空",groups = GroupInster.class)
    private String userName;

    @NotBlank(message = "密码不能为空",groups = GroupInster.class)
    private String password;

    private String nickName;

    @Email
    @NotBlank(message = "邮箱不能为空",groups = {GroupInster.class,GroupUpdate.class})
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    private String constellation;

    private String chineseZodiac;

    @NotBlank(message = "性别不能为空",groups = GroupUpdate.class)
    private String sex;

    private String image;

    private Integer deleteFlag;

}
