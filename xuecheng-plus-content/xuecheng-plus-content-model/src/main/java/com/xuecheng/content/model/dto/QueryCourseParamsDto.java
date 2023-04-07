package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryCourseParamsDto {
    @ApiModelProperty("审核状态")
    private String auditStatus;
    @ApiModelProperty("课程名称")
    private String courseName;
    @ApiModelProperty("发布状态")
    private String publishStatus;
}
