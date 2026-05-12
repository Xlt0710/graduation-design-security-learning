package com.security.learn.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;

    private String email;
}
