package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class XueChengPlusException extends RuntimeException{
    private String errMessage;
    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }
}
