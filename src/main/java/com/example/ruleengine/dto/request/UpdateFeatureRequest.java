package com.example.ruleengine.dto.request;

import com.example.ruleengine.entity.enums.FeatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeatureRequest {

    private String name;

    private String description;

    private FeatureType featureType;

    private Map<String, Object> extractionConfig;

    private Object defaultValue;
}
