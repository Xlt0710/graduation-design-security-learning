package com.security.learn.service;

import com.security.learn.dto.NoticeRequest;
import com.security.learn.dto.NoticeResponse;
import com.security.learn.dto.TagRequest;
import com.security.learn.dto.TagResponse;

import java.util.List;

public interface AdminService {

    List<NoticeResponse> getNotices();

    NoticeResponse createNotice(NoticeRequest request);

    NoticeResponse updateNotice(Long id, NoticeRequest request);

    void deleteNotice(Long id);

    List<TagResponse> getTags(String tagType);

    TagResponse createTag(TagRequest request);

    void deleteTag(Long id);
}
