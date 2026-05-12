package com.security.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab")
public class Lab {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String vulnerabilityType;

    private Integer difficulty;

    private String targetUrl;

    private String flag;

    private String hint;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
