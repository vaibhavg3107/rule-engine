package com.example.ruleengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeEvaluationResultResponse {
    private boolean result;
    private String nodeType;
    private String operator;
    private UUID ruleId;
    private String ruleName;
    private String featureName;
    private Object featureValue;
    private String operatorCode;
    private Object operand;
    private String failureReason;
    private List<TreeEvaluationResultResponse> childResults;
}
