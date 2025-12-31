package com.example.ruleengine.dto.request;

import com.example.ruleengine.entity.enums.FeatureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeatureRequest {

    @NotBlank(message = "Feature name is required")
    private String name;

    private String description;

    @NotNull(message = "Feature type is required")
    private FeatureType featureType;

    @NotNull(message = "Extraction config is required")
    private Map<String, Object> extractionConfig;

    private Object defaultValue;
}
