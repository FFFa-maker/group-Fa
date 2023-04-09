package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class RestErrorResponse implements Serializable {
    private String errMessage;
}
