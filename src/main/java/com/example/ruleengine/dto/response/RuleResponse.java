package com.example.ruleengine.dto.response;

import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.entity.enums.OperandType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID featureId;
    private String featureName;
    private FeatureType featureType;
    private String operatorCode;
    private String operatorName;
    private OperandType operandType;
    private Object operand;
    private Boolean enabled;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
