package com.example.ruleengine.service;

import com.example.ruleengine.dto.response.TestRuleResponse;
import com.example.ruleengine.entity.Rule;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleTestService {

    private final RuleRepository ruleRepository;
    private final FeatureExtractionService featureExtractionService;
    private final RuleEvaluationService ruleEvaluationService;

    public TestRuleResponse testRule(UUID ruleId, Map<String, Object> inputData) {
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule", "id", ruleId));

        Object extractedValue = null;
        boolean result = false;
        String message;

        try {
            Map<String, Object> extractedFeatures = featureExtractionService.extractFeatures(
                    Collections.singletonList(rule.getFeature()), inputData);
            
            extractedValue = extractedFeatures.get(rule.getFeature().getName());
            
            result = ruleEvaluationService.evaluateRule(rule, extractedValue);
            
            message = result ? "Rule passed" : "Rule failed";
        } catch (Exception e) {
            log.error("Error testing rule {}: {}", ruleId, e.getMessage());
            message = "Error: " + e.getMessage();
        }

        return TestRuleResponse.builder()
                .ruleName(rule.getName())
                .featureName(rule.getFeature().getName())
                .extractedValue(extractedValue)
                .operatorCode(rule.getOperator().getCode())
                .operatorName(rule.getOperator().getName())
                .operand(rule.getOperand())
                .result(result)
                .message(message)
                .build();
    }
}
