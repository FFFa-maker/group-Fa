package com.xuecheng.content.service.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;

public class ListUtils {
    public static <E, T> void copyProperties(List<E> eList, List<T> tList, Class<T> tClass) throws Exception{
        int n = eList.size();
        for (int i = 0; i < n; i++) {
            T t = tClass.newInstance();
            BeanUtils.copyProperties(eList.get(i), t);
            tList.add(t);
        }
    }
}
