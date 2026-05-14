package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private List<String> roles;
}
