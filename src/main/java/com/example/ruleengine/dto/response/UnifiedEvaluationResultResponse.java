package com.example.ruleengine.dto.response;

import com.example.ruleengine.entity.enums.EvaluationStrategy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedEvaluationResultResponse {
    // Internal fields for logging (not serialized to response)
    @JsonIgnore
    private UUID policySetId;
    @JsonIgnore
    private String policySetName;
    @JsonIgnore
    private EvaluationStrategy evaluationStrategy;
    @JsonIgnore
    private LocalDateTime evaluatedAt;
    @JsonIgnore
    private PolicyEvaluationResultResponse booleanResult;
    @JsonIgnore
    private PolicyEvaluationResultResponse offerResult;
    @JsonIgnore
    private String selectedOfferPolicyName;
    @JsonIgnore
    private Integer selectedOfferPolicyPriority;
    @JsonIgnore
    private List<OfferResultSummaryResponse> allOfferResults;

    // Response fields
    private DecisionResponse decision;
    private OfferResponse offer;
}
