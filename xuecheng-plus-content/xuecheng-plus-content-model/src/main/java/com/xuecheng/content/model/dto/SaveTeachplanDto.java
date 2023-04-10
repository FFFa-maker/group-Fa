package com.xuecheng.content.model.dto;

import lombok.Data;

@Data
public class SaveTeachplanDto {
    private Long id;
    private String pname;
    private Long parentid;
    private Integer grade;
    private String mediaType;
    private Long courseId;
    private Long coursePubId;
    private String isPreview;
}
