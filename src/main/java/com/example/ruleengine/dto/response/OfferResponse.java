package com.example.ruleengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {
    private Double loanAmount;
    private Double rateOfInterest;
    private Double processingFee;
    private Integer tenure;
    private Double emi;
}
