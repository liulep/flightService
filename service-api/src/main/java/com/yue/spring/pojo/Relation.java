package com.yue.spring.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.yue.data.annotation.Sensitive;
import com.yue.data.model.SensitiveStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Relation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    @NotBlank(message = "联系人姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phone;

    @NotBlank(message = "身份证号码不能为空")
    @Sensitive(strategy = SensitiveStrategy.ID_CARD)
    private String identity;

    @TableLogic
    private Integer deleteFlag;
}
