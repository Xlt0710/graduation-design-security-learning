package com.security.learn.dto;

import lombok.Data;

@Data
public class NoticeRequest {

    private String title;

    private String content;

    private Integer status;
}
