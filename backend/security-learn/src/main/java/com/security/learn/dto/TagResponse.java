package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagResponse {

    private Long id;
    private String tagName;
    private String tagType;
}
