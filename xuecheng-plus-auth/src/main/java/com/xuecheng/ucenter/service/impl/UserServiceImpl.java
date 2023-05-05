package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcMenuMapper xcMenuMapper;
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //转成AuthParamDto对象
        AuthParamsDto authParamsDto = null;
        try{
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch(Exception e){
            throw new RuntimeException("请求认证参数不符合要求");
        }
        String authType = authParamsDto.getAuthType();
        String beanName = authType+"_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        UserDetails userDetails = getUserPrincipal(xcUserExt);
        return userDetails;
    }

    public UserDetails getUserPrincipal(XcUserExt xcUser){
        String password = xcUser.getPassword();
        //获取权限
        String[] authorities = {"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if(xcMenus.size()>0){
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m->{
                permissions.add(m.getCode());
            });
            authorities = permissions.toArray(new String[0]);
        }
        xcUser.setPassword(null);
        String userJson = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
        return userDetails;
    }
}
