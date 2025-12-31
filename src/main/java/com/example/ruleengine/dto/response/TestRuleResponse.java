package com.example.ruleengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRuleResponse {

    private String ruleName;
    private String featureName;
    private Object extractedValue;
    private String operatorCode;
    private String operatorName;
    private Object operand;
    private boolean result;
    private String message;
}
