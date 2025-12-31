package com.example.ruleengine.service;

import com.example.ruleengine.dto.response.TreeEvaluationResultResponse;
import com.example.ruleengine.entity.Rule;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreeEvaluationService {

    private final RuleRepository ruleRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public TreeEvaluationResultResponse evaluateTree(Map<String, Object> node, Map<String, Object> extractedFeatures) {
        String nodeType = (String) node.get("type");
        
        if ("LEAF".equals(nodeType)) {
            return evaluateLeafNode(node, extractedFeatures);
        } else if ("COMPOSITE".equals(nodeType)) {
            return evaluateCompositeNode(node, extractedFeatures);
        } else {
            throw new ValidationException("Unknown node type: " + nodeType);
        }
    }

    private TreeEvaluationResultResponse evaluateLeafNode(Map<String, Object> node, Map<String, Object> extractedFeatures) {
        String ruleIdStr = (String) node.get("ruleId");
        UUID ruleId = UUID.fromString(ruleIdStr);
        
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ValidationException("Rule not found: " + ruleId));
        
        String featureName = rule.getFeature().getName();
        Object featureValue = extractedFeatures.get(featureName);
        
        boolean result = ruleEvaluationService.evaluateRule(rule, featureValue);
        
        TreeEvaluationResultResponse evalResult = new TreeEvaluationResultResponse();
        evalResult.setResult(result);
        evalResult.setNodeType("LEAF");
        evalResult.setRuleId(ruleId);
        evalResult.setRuleName(rule.getName());
        evalResult.setFeatureName(featureName);
        evalResult.setFeatureValue(featureValue);
        evalResult.setOperatorCode(rule.getOperator().getCode());
        evalResult.setOperand(rule.getOperand());
        
        if (!result) {
            evalResult.setFailureReason(String.format("Rule '%s' failed: %s %s %s = false",
                    rule.getName(), featureValue, rule.getOperator().getCode(), rule.getOperand()));
        }
        
        return evalResult;
    }

    @SuppressWarnings("unchecked")
    private TreeEvaluationResultResponse evaluateCompositeNode(Map<String, Object> node, Map<String, Object> extractedFeatures) {
        String operator = (String) node.get("operator");
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        
        TreeEvaluationResultResponse result = new TreeEvaluationResultResponse();
        result.setNodeType("COMPOSITE");
        result.setOperator(operator);
        result.setChildResults(new ArrayList<>());
        
        switch (operator) {
            case "AND":
                result.setResult(evaluateAnd(children, extractedFeatures, result));
                break;
            case "OR":
                result.setResult(evaluateOr(children, extractedFeatures, result));
                break;
            case "NOT":
                result.setResult(evaluateNot(children.get(0), extractedFeatures, result));
                break;
            default:
                throw new ValidationException("Unknown operator: " + operator);
        }
        
        return result;
    }

    private boolean evaluateAnd(List<Map<String, Object>> children, Map<String, Object> extractedFeatures, TreeEvaluationResultResponse parentResult) {
        List<String> failureReasons = new ArrayList<>();
        boolean allPassed = true;
        
        for (Map<String, Object> child : children) {
            TreeEvaluationResultResponse childResult = evaluateTree(child, extractedFeatures);
            parentResult.getChildResults().add(childResult);
            
            if (!childResult.isResult()) {
                allPassed = false;
                if (childResult.getFailureReason() != null) {
                    failureReasons.add(childResult.getFailureReason());
                }
            }
        }
        
        if (!allPassed) {
            parentResult.setFailureReason(String.join("; ", failureReasons));
        }
        
        return allPassed;
    }

    private boolean evaluateOr(List<Map<String, Object>> children, Map<String, Object> extractedFeatures, TreeEvaluationResultResponse parentResult) {
        List<String> failureReasons = new ArrayList<>();
        boolean anyPassed = false;
        
        for (Map<String, Object> child : children) {
            TreeEvaluationResultResponse childResult = evaluateTree(child, extractedFeatures);
            parentResult.getChildResults().add(childResult);
            
            if (childResult.isResult()) {
                anyPassed = true;
            } else if (childResult.getFailureReason() != null) {
                failureReasons.add(childResult.getFailureReason());
            }
        }
        
        if (!anyPassed) {
            parentResult.setFailureReason("All OR conditions failed: " + String.join("; ", failureReasons));
        }
        
        return anyPassed;
    }

    private boolean evaluateNot(Map<String, Object> child, Map<String, Object> extractedFeatures, TreeEvaluationResultResponse parentResult) {
        TreeEvaluationResultResponse childResult = evaluateTree(child, extractedFeatures);
        parentResult.getChildResults().add(childResult);
        
        boolean result = !childResult.isResult();
        if (!result) {
            parentResult.setFailureReason("NOT condition failed: inner condition was true");
        }
        
        return result;
    }
}
