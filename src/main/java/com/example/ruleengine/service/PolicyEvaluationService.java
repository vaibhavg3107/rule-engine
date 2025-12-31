package com.example.ruleengine.service;

import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.Rule;
import com.example.ruleengine.entity.enums.PolicyType;
import com.example.ruleengine.repository.FeatureRepository;
import com.example.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyEvaluationService {

    private final PolicyService policyService;
    private final FeatureRepository featureRepository;
    private final RuleRepository ruleRepository;
    private final FeatureExtractionService featureExtractionService;
    private final TreeEvaluationService treeEvaluationService;

    public PolicyEvaluationResult evaluatePolicy(UUID policyId, Map<String, Object> inputData) {
        Policy policy = policyService.getPolicyEntityById(policyId);
        
        Set<UUID> ruleIds = collectRuleIds(policy.getRootNode());
        List<Rule> rules = ruleRepository.findAllById(ruleIds);
        
        Set<UUID> featureIds = rules.stream()
                .map(r -> r.getFeature().getId())
                .collect(Collectors.toSet());
        List<Feature> features = featureRepository.findAllById(featureIds);
        
        Map<String, Object> extractedFeatures = featureExtractionService.extractFeatures(features, inputData);
        
        TreeEvaluationService.TreeEvaluationResult treeResult = 
                treeEvaluationService.evaluateTree(policy.getRootNode(), extractedFeatures);
        
        PolicyEvaluationResult result = new PolicyEvaluationResult();
        result.setPolicyId(policyId);
        result.setPolicyName(policy.getName());
        result.setPolicyType(policy.getPolicyType());
        result.setExtractedFeatures(extractedFeatures);
        result.setTreeResult(treeResult);
        
        if (policy.getPolicyType() == PolicyType.BOOLEAN) {
            result.setDecision(buildBooleanDecision(treeResult));
        } else if (policy.getPolicyType() == PolicyType.OFFER) {
            result.setDecision(buildBooleanDecision(treeResult));
            if (treeResult.isResult()) {
                result.setOffer(buildOffer(policy.getOutputMapping(), extractedFeatures, treeResult));
            }
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> collectRuleIds(Map<String, Object> node) {
        Set<UUID> ruleIds = new HashSet<>();
        String nodeType = (String) node.get("type");
        
        if ("LEAF".equals(nodeType)) {
            String ruleIdStr = (String) node.get("ruleId");
            ruleIds.add(UUID.fromString(ruleIdStr));
        } else if ("COMPOSITE".equals(nodeType)) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            for (Map<String, Object> child : children) {
                ruleIds.addAll(collectRuleIds(child));
            }
        }
        
        return ruleIds;
    }

    private Decision buildBooleanDecision(TreeEvaluationService.TreeEvaluationResult treeResult) {
        Decision decision = new Decision();
        decision.setStatus(treeResult.isResult() ? "APPROVED" : "REJECTED");
        
        if (!treeResult.isResult()) {
            decision.setReasons(collectFailureReasons(treeResult));
        }
        
        return decision;
    }

    private List<String> collectFailureReasons(TreeEvaluationService.TreeEvaluationResult result) {
        List<String> reasons = new ArrayList<>();
        
        if (result.getFailureReason() != null && "LEAF".equals(result.getNodeType())) {
            reasons.add(result.getFailureReason());
        }
        
        if (result.getChildResults() != null) {
            for (TreeEvaluationService.TreeEvaluationResult child : result.getChildResults()) {
                reasons.addAll(collectFailureReasons(child));
            }
        }
        
        return reasons;
    }

    @SuppressWarnings("unchecked")
    private Offer buildOffer(Map<String, Object> outputMapping, Map<String, Object> extractedFeatures, 
                             TreeEvaluationService.TreeEvaluationResult treeResult) {
        Offer offer = new Offer();
        
        if (outputMapping == null) {
            return offer;
        }
        
        Map<String, Object> defaultOutput = (Map<String, Object>) outputMapping.get("defaultOutput");
        if (defaultOutput != null) {
            applyOfferValues(offer, defaultOutput);
        }
        
        List<Map<String, Object>> conditionalOutputs = (List<Map<String, Object>>) outputMapping.get("conditionalOutputs");
        if (conditionalOutputs != null) {
            for (Map<String, Object> conditional : conditionalOutputs) {
                String condition = (String) conditional.get("condition");
                if (evaluateCondition(condition, extractedFeatures)) {
                    Map<String, Object> output = (Map<String, Object>) conditional.get("output");
                    applyOfferValues(offer, output);
                    break;
                }
            }
        }
        
        return offer;
    }

    private void applyOfferValues(Offer offer, Map<String, Object> values) {
        if (values.containsKey("loanAmount")) {
            offer.setLoanAmount(((Number) values.get("loanAmount")).doubleValue());
        }
        if (values.containsKey("rateOfInterest")) {
            offer.setRateOfInterest(((Number) values.get("rateOfInterest")).doubleValue());
        }
        if (values.containsKey("processingFee")) {
            offer.setProcessingFee(((Number) values.get("processingFee")).doubleValue());
        }
        if (values.containsKey("tenure")) {
            offer.setTenure(((Number) values.get("tenure")).intValue());
        }
        if (values.containsKey("emi")) {
            offer.setEmi(((Number) values.get("emi")).doubleValue());
        }
    }

    private boolean evaluateCondition(String condition, Map<String, Object> extractedFeatures) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        
        if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            String featureName = parts[0].trim();
            double threshold = Double.parseDouble(parts[1].trim());
            Object value = extractedFeatures.get(featureName);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() >= threshold;
            }
        } else if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            String featureName = parts[0].trim();
            double threshold = Double.parseDouble(parts[1].trim());
            Object value = extractedFeatures.get(featureName);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() <= threshold;
            }
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            String featureName = parts[0].trim();
            double threshold = Double.parseDouble(parts[1].trim());
            Object value = extractedFeatures.get(featureName);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() > threshold;
            }
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            String featureName = parts[0].trim();
            double threshold = Double.parseDouble(parts[1].trim());
            Object value = extractedFeatures.get(featureName);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() < threshold;
            }
        }
        
        return false;
    }

    @lombok.Data
    public static class PolicyEvaluationResult {
        private UUID policyId;
        private String policyName;
        private PolicyType policyType;
        private Map<String, Object> extractedFeatures;
        private TreeEvaluationService.TreeEvaluationResult treeResult;
        private Decision decision;
        private Offer offer;
    }

    @lombok.Data
    public static class Decision {
        private String status;
        private List<String> reasons;
    }

    @lombok.Data
    public static class Offer {
        private Double loanAmount;
        private Double rateOfInterest;
        private Double processingFee;
        private Integer tenure;
        private Double emi;
    }
}
