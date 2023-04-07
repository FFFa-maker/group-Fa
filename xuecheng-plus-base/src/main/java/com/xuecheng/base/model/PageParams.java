package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageParams {
    //当前页码
    @ApiModelProperty("页码")
    private Long pageNo = 1L;
    //页面大小
    @ApiModelProperty("每页大小")
    private Long pageSize = 10L;

}
