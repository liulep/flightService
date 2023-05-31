package com.yue.spring.pojo;

import com.google.errorprone.annotations.NoAllocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock implements Serializable {

    private static final long serialVersionUID=986823857621547285L;

    private Integer id;

    private Integer lineId;

    private Integer stock;

}
