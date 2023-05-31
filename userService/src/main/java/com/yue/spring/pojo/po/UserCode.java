package com.yue.spring.pojo.po;

import com.yue.spring.group.GroupInster;
import com.yue.spring.group.GroupUpdate;
import com.yue.spring.pojo.User;
import lombok.*;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCode extends User {


    @NotBlank(message = "验证码不能为空",groups = GroupInster.class)
    private String code;

    private String newPassword;
}
