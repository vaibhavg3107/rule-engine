package com.example.ruleengine.service;

import com.example.ruleengine.dto.request.CreateRuleRequest;
import com.example.ruleengine.dto.response.RuleResponse;
import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.Operator;
import com.example.ruleengine.entity.Rule;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.FeatureRepository;
import com.example.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final FeatureRepository featureRepository;
    private final OperatorService operatorService;

    @Transactional
    public RuleResponse createRule(CreateRuleRequest request) {
        Feature feature = featureRepository.findById(request.getFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", request.getFeatureId()));

        Operator operator = operatorService.getOperatorEntityByCode(request.getOperatorCode());

        validateOperatorCompatibility(feature, operator);
        validateOperand(operator, request.getOperand());

        Rule rule = Rule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .feature(feature)
                .operator(operator)
                .operand(request.getOperand())
                .build();

        Rule savedRule = ruleRepository.save(rule);
        return mapToResponse(savedRule);
    }

    public RuleResponse getRuleById(UUID id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule", "id", id));
        return mapToResponse(rule);
    }

    public List<RuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RuleResponse> getRulesByFeatureId(UUID featureId) {
        return ruleRepository.findByFeatureId(featureId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RuleResponse updateRule(UUID id, CreateRuleRequest request) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule", "id", id));

        if (request.getName() != null) {
            rule.setName(request.getName());
        }

        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }

        if (request.getFeatureId() != null) {
            Feature feature = featureRepository.findById(request.getFeatureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", request.getFeatureId()));
            rule.setFeature(feature);
        }

        if (request.getOperatorCode() != null) {
            Operator operator = operatorService.getOperatorEntityByCode(request.getOperatorCode());
            validateOperatorCompatibility(rule.getFeature(), operator);
            rule.setOperator(operator);
        }

        if (request.getOperand() != null) {
            validateOperand(rule.getOperator(), request.getOperand());
            rule.setOperand(request.getOperand());
        }

        rule.setVersion(rule.getVersion() + 1);
        Rule updatedRule = ruleRepository.save(rule);
        return mapToResponse(updatedRule);
    }

    @Transactional
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rule", "id", id);
        }
        ruleRepository.deleteById(id);
    }

    private void validateOperatorCompatibility(Feature feature, Operator operator) {
        String featureType = feature.getFeatureType().name();
        boolean isCompatible = operator.getCompatibleFeatureTypes().contains(featureType);
        
        if (!isCompatible) {
            throw new ValidationException(
                    String.format("Operator '%s' is not compatible with feature type '%s'. Compatible types: %s",
                            operator.getCode(),
                            featureType,
                            operator.getCompatibleFeatureTypes()));
        }
    }

    private void validateOperand(Operator operator, Object operand) {
        switch (operator.getOperandType()) {
            case NONE:
                if (operand != null) {
                    throw new ValidationException(
                            String.format("Operator '%s' does not require an operand", operator.getCode()));
                }
                break;
            case SINGLE:
                if (operand == null) {
                    throw new ValidationException(
                            String.format("Operator '%s' requires a single operand value", operator.getCode()));
                }
                break;
            case LIST:
                if (operand == null || !(operand instanceof List)) {
                    throw new ValidationException(
                            String.format("Operator '%s' requires a list operand", operator.getCode()));
                }
                break;
            case RANGE:
                if (operand == null || !(operand instanceof java.util.Map)) {
                    throw new ValidationException(
                            String.format("Operator '%s' requires a range operand with 'min' and 'max' fields", operator.getCode()));
                }
                break;
        }
    }

    private RuleResponse mapToResponse(Rule rule) {
        return RuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .featureId(rule.getFeature().getId())
                .featureName(rule.getFeature().getName())
                .featureType(rule.getFeature().getFeatureType())
                .operatorCode(rule.getOperator().getCode())
                .operatorName(rule.getOperator().getName())
                .operandType(rule.getOperator().getOperandType())
                .operand(rule.getOperand())
                .enabled(rule.getEnabled())
                .version(rule.getVersion())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
