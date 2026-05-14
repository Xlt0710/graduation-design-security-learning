package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecommendationResponse {

    private List<RecommendedItem> courses;
    private List<RecommendedItem> labs;
    private String suggestion;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecommendedItem {
        private Long id;
        private String title;
        private String reason;
        private BigDecimal score;
        private String type;
        private Integer difficulty;
    }
}
