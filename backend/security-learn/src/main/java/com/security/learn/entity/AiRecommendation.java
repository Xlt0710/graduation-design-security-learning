package com.security.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_recommendation")
public class AiRecommendation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String recommendType;

    private Long targetId;

    private String reason;

    private BigDecimal score;

    private LocalDateTime createdAt;
}
