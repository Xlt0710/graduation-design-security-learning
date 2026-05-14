package com.security.learn.service;

import com.security.learn.dto.NoticeRequest;
import com.security.learn.dto.NoticeResponse;
import com.security.learn.dto.TagRequest;
import com.security.learn.dto.TagResponse;

import java.util.List;

public interface AdminService {

    List<NoticeResponse> getNotices();

    NoticeResponse createNotice(Long userId, NoticeRequest request);

    NoticeResponse updateNotice(Long id, Long userId, NoticeRequest request);

    void deleteNotice(Long id, Long userId);

    List<TagResponse> getTags(String tagType);

    TagResponse createTag(Long userId, TagRequest request);

    void deleteTag(Long id, Long userId);
}
