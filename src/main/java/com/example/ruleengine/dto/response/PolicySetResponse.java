package com.example.ruleengine.dto.response;

import com.example.ruleengine.entity.enums.EvaluationStrategy;
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
public class PolicySetResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID booleanPolicyId;
    private String booleanPolicyName;
    private List<OfferPolicyInfo> offerPolicies;
    private EvaluationStrategy evaluationStrategy;
    private Boolean enabled;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfferPolicyInfo {
        private UUID policyId;
        private String policyName;
        private Integer priority;
        private Boolean enabled;
    }
}
