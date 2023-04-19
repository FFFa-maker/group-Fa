package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
    @Override
    public void saveTeachPlan(SaveTeachplanDto dto){
        Long id = dto.getId();
        if(id!=null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(dto, teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            int count = getTeachplanCount(dto.getCourseId(), dto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            teachplanNew.setOrderby(count+1);
            BeanUtils.copyProperties(dto, teachplanNew);
            teachplanMapper.insert(teachplanNew);
        }
    }

    @Override
    public void deleteTeachplan(long id){
        Teachplan teachplan = teachplanMapper.selectById(id);
        //一级章
        if(teachplan.getGrade()==1){
            int childCount = getChildCount(id);
            if(childCount>0){

                XueChengPlusException.cast("课程计划还有子级信息无法操作");
            }else{
                teachplanMapper.deleteById(id);
            }
        }
        //二级章节
        else{
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, id);
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
            if(teachplanMedia!=null){
                teachplanMediaMapper.delete(queryWrapper);
            }
            teachplanMapper.deleteById(id);
        }
    }

    @Override
    public void moveTeachplan(String move, long id){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        Teachplan teachplan = teachplanMapper.selectById(id);
        queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        List<Teachplan> list = null;
        if(teachplan!=null){
            if(move.equals("moveup")){
                queryWrapper.lt(Teachplan::getOrderby, teachplan.getOrderby());
                list = teachplanMapper.selectList(queryWrapper);
                list.sort((a, b)->b.getOrderby()-a.getOrderby());
            }else if(move.equals("movedown")){
                queryWrapper.gt(Teachplan::getOrderby, teachplan.getOrderby());
                list = teachplanMapper.selectList(queryWrapper);
                list.sort((a,b)->a.getOrderby()-b.getOrderby());
            }else{
                XueChengPlusException.cast("移动方向不正确");
            }
            if(list.size()==0){
                XueChengPlusException.cast("无法移动至更边缘");
            }else{
                Teachplan teachplanSwap = list.get(0);
                int temp = teachplan.getOrderby();
                teachplan.setOrderby(teachplanSwap.getOrderby());
                teachplanSwap.setOrderby(temp);
                teachplanMapper.updateById(teachplan);
                teachplanMapper.updateById(teachplanSwap);
            }
        }else {
            XueChengPlusException.cast("未查询到该移动课程");
        }

    }

    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        Long courseId = teachplan.getCourseId();
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public void deleteMedia(Long teachPlanId, String mediaId){
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachPlanId)
                .eq(TeachplanMedia::getMediaId, mediaId));
    }

    private int getChildCount(long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentId);
        int count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }

    private int getTeachplanCount(long courseId, long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        List<Teachplan> list = teachplanMapper.selectList(queryWrapper);
        final int[] max = {0};
        list.stream().forEach(item-> max[0] = Math.max(max[0], item.getOrderby()));
        int count = teachplanMapper.selectCount(queryWrapper);
        return max[0];
    }

}
