package com.example.ruleengine.service;

import com.example.ruleengine.dto.response.OfferResultSummaryResponse;
import com.example.ruleengine.dto.response.PolicyEvaluationResultResponse;
import com.example.ruleengine.dto.response.UnifiedEvaluationResultResponse;
import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.PolicySet;
import com.example.ruleengine.entity.PolicySetExecutionLog;
import com.example.ruleengine.entity.PolicySetOfferPolicy;
import com.example.ruleengine.repository.PolicySetExecutionLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedEvaluationService {

    private final PolicySetService policySetService;
    private final PolicyEvaluationService policyEvaluationService;
    private final PolicySetExecutionLogRepository executionLogRepository;
    private final ObjectMapper objectMapper;

    public UnifiedEvaluationResultResponse evaluate(UUID policySetId, Map<String, Object> inputData) {
        long startTime = System.currentTimeMillis();
        
        PolicySet policySet = policySetService.getPolicySetEntityById(policySetId);
        
        UnifiedEvaluationResultResponse result = new UnifiedEvaluationResultResponse();
        result.setPolicySetId(policySetId);
        result.setPolicySetName(policySet.getName());
        result.setEvaluationStrategy(policySet.getEvaluationStrategy());
        result.setEvaluatedAt(LocalDateTime.now());

        Policy booleanPolicy = policySet.getBooleanPolicy();
        List<PolicySetOfferPolicy> offerPolicies = policySet.getOfferPolicies();

        switch (policySet.getEvaluationStrategy()) {
            case BOOLEAN_FIRST:
                evaluateBooleanFirst(result, policySet, booleanPolicy, offerPolicies, inputData);
                break;
            case OFFER_FIRST:
                evaluateOfferFirst(result, policySet, booleanPolicy, offerPolicies, inputData);
                break;
            case PARALLEL:
                evaluateParallel(result, policySet, booleanPolicy, offerPolicies, inputData);
                break;
            default:
                evaluateBooleanFirst(result, policySet, booleanPolicy, offerPolicies, inputData);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logExecution(policySet, inputData, result, (int) executionTime);

        return result;
    }

    private void logExecution(PolicySet policySet, Map<String, Object> inputData, 
                              UnifiedEvaluationResultResponse result, int executionTimeMs) {
        try {
            Map<String, Object> extractedFeatures = new HashMap<>();
            if (result.getBooleanResult() != null && result.getBooleanResult().getExtractedFeatures() != null) {
                extractedFeatures.putAll(result.getBooleanResult().getExtractedFeatures());
            }
            if (result.getOfferResult() != null && result.getOfferResult().getExtractedFeatures() != null) {
                extractedFeatures.putAll(result.getOfferResult().getExtractedFeatures());
            }

            PolicySetExecutionLog executionLog = PolicySetExecutionLog.builder()
                    .policySetId(policySet.getId())
                    .policySetVersion(policySet.getVersion())
                    .inputData(inputData)
                    .extractedFeatures(extractedFeatures)
                    .booleanPolicyResult(convertToMap(result.getBooleanResult()))
                    .offerPolicyResult(convertToMap(result.getOfferResult()))
                    .decisionStatus(result.getDecision() != null ? result.getDecision().getStatus() : null)
                    .executionTimeMs(executionTimeMs)
                    .executedAt(result.getEvaluatedAt())
                    .build();

            executionLogRepository.save(executionLog);
            log.debug("Logged execution for PolicySet {} with decision {}", 
                    policySet.getId(), result.getDecision() != null ? result.getDecision().getStatus() : "N/A");
        } catch (Exception e) {
            log.error("Failed to log execution for PolicySet {}: {}", policySet.getId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            log.warn("Failed to convert object to map: {}", e.getMessage());
            return null;
        }
    }

    private void evaluateBooleanFirst(UnifiedEvaluationResultResponse result, PolicySet policySet,
                                       Policy booleanPolicy, List<PolicySetOfferPolicy> offerPolicies,
                                       Map<String, Object> inputData) {
        if (booleanPolicy != null) {
            PolicyEvaluationResultResponse booleanResult = 
                    policyEvaluationService.evaluatePolicy(booleanPolicy.getId(), inputData);
            result.setBooleanResult(booleanResult);
            result.setDecision(booleanResult.getDecision());

            if ("APPROVED".equals(booleanResult.getDecision().getStatus())) {
                evaluateOfferPoliciesWithPriority(result, offerPolicies, inputData);
            }
        } else {
            evaluateOfferPoliciesWithPriority(result, offerPolicies, inputData);
            if (result.getOfferResult() != null) {
                result.setDecision(result.getOfferResult().getDecision());
            }
        }
    }

    private void evaluateOfferFirst(UnifiedEvaluationResultResponse result, PolicySet policySet,
                                     Policy booleanPolicy, List<PolicySetOfferPolicy> offerPolicies,
                                     Map<String, Object> inputData) {
        evaluateOfferPoliciesWithPriority(result, offerPolicies, inputData);

        if (result.getOfferResult() != null && "APPROVED".equals(result.getOfferResult().getDecision().getStatus())) {
            if (booleanPolicy != null) {
                PolicyEvaluationResultResponse booleanResult = 
                        policyEvaluationService.evaluatePolicy(booleanPolicy.getId(), inputData);
                result.setBooleanResult(booleanResult);
                result.setDecision(booleanResult.getDecision());
            } else {
                result.setDecision(result.getOfferResult().getDecision());
            }
        } else if (booleanPolicy != null) {
            PolicyEvaluationResultResponse booleanResult = 
                    policyEvaluationService.evaluatePolicy(booleanPolicy.getId(), inputData);
            result.setBooleanResult(booleanResult);
            result.setDecision(booleanResult.getDecision());
        } else if (result.getOfferResult() != null) {
            result.setDecision(result.getOfferResult().getDecision());
        }
    }

    private void evaluateParallel(UnifiedEvaluationResultResponse result, PolicySet policySet,
                                   Policy booleanPolicy, List<PolicySetOfferPolicy> offerPolicies,
                                   Map<String, Object> inputData) {
        PolicyEvaluationResultResponse booleanResult = null;

        if (booleanPolicy != null) {
            booleanResult = policyEvaluationService.evaluatePolicy(booleanPolicy.getId(), inputData);
            result.setBooleanResult(booleanResult);
        }

        evaluateOfferPoliciesWithPriority(result, offerPolicies, inputData);

        if (booleanResult != null) {
            result.setDecision(booleanResult.getDecision());
            if ("APPROVED".equals(booleanResult.getDecision().getStatus()) && result.getOfferResult() != null) {
                result.setOffer(result.getOfferResult().getOffer());
            }
        } else if (result.getOfferResult() != null) {
            result.setDecision(result.getOfferResult().getDecision());
        }
    }

    private void evaluateOfferPoliciesWithPriority(UnifiedEvaluationResultResponse result,
                                                    List<PolicySetOfferPolicy> offerPolicies,
                                                    Map<String, Object> inputData) {
        List<OfferPolicyEvaluationResult> allOfferResults = new ArrayList<>();

        if (offerPolicies != null && !offerPolicies.isEmpty()) {
            for (PolicySetOfferPolicy psop : offerPolicies) {
                if (psop.getEnabled() == null || psop.getEnabled()) {
                    try {
                        PolicyEvaluationResultResponse evalResult = 
                                policyEvaluationService.evaluatePolicy(psop.getOfferPolicy().getId(), inputData);
                        allOfferResults.add(new OfferPolicyEvaluationResult(
                                psop.getPriority(),
                                psop.getOfferPolicy().getName(),
                                evalResult
                        ));
                    } catch (Exception e) {
                        log.warn("Failed to evaluate offer policy {}: {}", 
                                psop.getOfferPolicy().getName(), e.getMessage());
                    }
                }
            }
        }

        if (!allOfferResults.isEmpty()) {
            allOfferResults.sort((a, b) -> Integer.compare(b.priority, a.priority));

            OfferPolicyEvaluationResult bestResult = null;
            for (OfferPolicyEvaluationResult offerEval : allOfferResults) {
                if ("APPROVED".equals(offerEval.result.getDecision().getStatus()) 
                        && offerEval.result.getOffer() != null) {
                    bestResult = offerEval;
                    break;
                }
            }

            if (bestResult == null && !allOfferResults.isEmpty()) {
                bestResult = allOfferResults.get(0);
            }

            if (bestResult != null) {
                result.setOfferResult(bestResult.result);
                result.setOffer(bestResult.result.getOffer());
                result.setSelectedOfferPolicyName(bestResult.policyName);
                result.setSelectedOfferPolicyPriority(bestResult.priority);
            }

            result.setAllOfferResults(allOfferResults.stream()
                    .map(r -> new OfferResultSummaryResponse(r.policyName, r.priority, 
                            r.result.getDecision().getStatus(), r.result.getOffer()))
                    .collect(java.util.stream.Collectors.toList()));
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class OfferPolicyEvaluationResult {
        private int priority;
        private String policyName;
        private PolicyEvaluationResultResponse result;
    }
}
