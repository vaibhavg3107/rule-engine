package com.example.ruleengine.dto.response;

import com.example.ruleengine.entity.enums.PolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEvaluationResultResponse {
    private UUID policyId;
    private String policyName;
    private PolicyType policyType;
    private Map<String, Object> extractedFeatures;
    private TreeEvaluationResultResponse treeResult;
    private DecisionResponse decision;
    private OfferResponse offer;
}
