package com.example.ruleengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResultSummaryResponse {
    private String policyName;
    private Integer priority;
    private String decisionStatus;
    private OfferResponse offer;
}
