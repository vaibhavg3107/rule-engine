package com.example.ruleengine.service;

import com.example.ruleengine.dto.response.OperatorResponse;
import com.example.ruleengine.entity.Operator;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorRepository operatorRepository;

    public List<OperatorResponse> getAllOperators() {
        return operatorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OperatorResponse getOperatorByCode(String code) {
        Operator operator = operatorRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", "code", code));
        return mapToResponse(operator);
    }

    public List<OperatorResponse> getOperatorsByFeatureType(FeatureType featureType) {
        String jsonArray = "[\"" + featureType.name() + "\"]";
        return operatorRepository.findByCompatibleFeatureType(jsonArray).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Operator getOperatorEntityByCode(String code) {
        return operatorRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", "code", code));
    }

    private OperatorResponse mapToResponse(Operator operator) {
        return OperatorResponse.builder()
                .id(operator.getId())
                .code(operator.getCode())
                .name(operator.getName())
                .description(operator.getDescription())
                .compatibleFeatureTypes(operator.getCompatibleFeatureTypes())
                .operandType(operator.getOperandType())
                .operandElementType(operator.getOperandElementType())
                .build();
    }
}
