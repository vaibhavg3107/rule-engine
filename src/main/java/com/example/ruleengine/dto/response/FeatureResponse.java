package com.example.ruleengine.dto.response;

import com.example.ruleengine.entity.enums.FeatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResponse {

    private UUID id;
    private String name;
    private String description;
    private FeatureType featureType;
    private Map<String, Object> extractionConfig;
    private Object defaultValue;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
