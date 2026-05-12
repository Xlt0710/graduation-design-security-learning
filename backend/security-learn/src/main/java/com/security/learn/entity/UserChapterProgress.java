package com.security.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_chapter_progress")
public class UserChapterProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long chapterId;

    private Integer status;

    private LocalDateTime completedAt;
}
