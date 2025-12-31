package com.example.ruleengine.dto.request;

import com.example.ruleengine.entity.enums.EvaluationStrategy;
import jakarta.validation.constraints.NotBlank;
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
public class CreatePolicySetRequest {

    @NotBlank(message = "PolicySet name is required")
    private String name;

    private String description;

    private UUID booleanPolicyId;

    private List<OfferPolicyWithPriority> offerPolicies;

    @Builder.Default
    private EvaluationStrategy evaluationStrategy = EvaluationStrategy.BOOLEAN_FIRST;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfferPolicyWithPriority {
        private UUID policyId;
        private Integer priority;
        @Builder.Default
        private Boolean enabled = true;
    }
}
