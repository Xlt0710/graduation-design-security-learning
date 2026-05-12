package com.security.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab_favorite")
public class LabFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long labId;

    private LocalDateTime createdAt;
}
