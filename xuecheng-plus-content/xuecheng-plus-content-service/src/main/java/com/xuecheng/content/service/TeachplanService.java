package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

public interface TeachplanService {
    public List<TeachplanDto> findTeachplanTree(long courseId);
}
