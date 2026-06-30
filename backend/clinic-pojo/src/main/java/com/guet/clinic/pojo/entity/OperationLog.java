package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OperationLog extends BaseEntity {
    private String operator;
    private String module;
    private String operation;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private Long duration;
    private String result;
    private String errorMessage;
}
