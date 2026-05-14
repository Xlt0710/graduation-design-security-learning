package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.common.RoleConstants;
import com.security.learn.dto.NoticeRequest;
import com.security.learn.dto.NoticeResponse;
import com.security.learn.dto.TagRequest;
import com.security.learn.dto.TagResponse;
import com.security.learn.entity.Notice;
import com.security.learn.entity.Role;
import com.security.learn.entity.Tag;
import com.security.learn.entity.UserRole;
import com.security.learn.mapper.NoticeMapper;
import com.security.learn.mapper.RoleMapper;
import com.security.learn.mapper.TagMapper;
import com.security.learn.mapper.UserRoleMapper;
import com.security.learn.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final NoticeMapper noticeMapper;
    private final TagMapper tagMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public AdminServiceImpl(NoticeMapper noticeMapper, TagMapper tagMapper,
                            RoleMapper roleMapper, UserRoleMapper userRoleMapper) {
        this.noticeMapper = noticeMapper;
        this.tagMapper = tagMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    private void checkAdmin(Long userId) {
        Role adminRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, RoleConstants.ADMIN));
        if (adminRole == null) {
            throw new IllegalArgumentException("系统错误：管理员角色不存在");
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, adminRole.getId()));
        if (count == 0) {
            throw new IllegalArgumentException("权限不足，仅管理员可操作");
        }
    }

    @Override
    public List<NoticeResponse> getNotices() {
        return noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreatedAt))
                .stream()
                .map(n -> new NoticeResponse(
                        n.getId(), n.getTitle(), n.getContent(), n.getStatus(),
                        n.getCreatedAt(), n.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoticeResponse createNotice(Long userId, NoticeRequest request) {
        checkAdmin(userId);
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.insert(notice);

        log.info("管理员创建通知: id={}, title={}", notice.getId(), notice.getTitle());
        return new NoticeResponse(notice.getId(), notice.getTitle(), notice.getContent(),
                notice.getStatus(), notice.getCreatedAt(), notice.getUpdatedAt());
    }

    @Override
    @Transactional
    public NoticeResponse updateNotice(Long id, Long userId, NoticeRequest request) {
        checkAdmin(userId);
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new IllegalArgumentException("通知不存在");
        }
        if (StringUtils.hasText(request.getTitle())) notice.setTitle(request.getTitle());
        if (request.getContent() != null) notice.setContent(request.getContent());
        if (request.getStatus() != null) notice.setStatus(request.getStatus());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);

        return new NoticeResponse(notice.getId(), notice.getTitle(), notice.getContent(),
                notice.getStatus(), notice.getCreatedAt(), notice.getUpdatedAt());
    }

    @Override
    @Transactional
    public void deleteNotice(Long id, Long userId) {
        checkAdmin(userId);
        noticeMapper.deleteById(id);
        log.info("管理员删除通知: id={}", id);
    }

    @Override
    public List<TagResponse> getTags(String tagType) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tagType)) {
            wrapper.eq(Tag::getTagType, tagType);
        }
        return tagMapper.selectList(wrapper).stream()
                .map(t -> new TagResponse(t.getId(), t.getTagName(), t.getTagType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagResponse createTag(Long userId, TagRequest request) {
        checkAdmin(userId);
        Tag tag = new Tag();
        tag.setTagName(request.getTagName());
        tag.setTagType(request.getTagType());
        tagMapper.insert(tag);

        log.info("管理员创建标签: id={}, name={}", tag.getId(), tag.getTagName());
        return new TagResponse(tag.getId(), tag.getTagName(), tag.getTagType());
    }

    @Override
    @Transactional
    public void deleteTag(Long id, Long userId) {
        checkAdmin(userId);
        tagMapper.deleteById(id);
        log.info("管理员删除标签: id={}", id);
    }
}
