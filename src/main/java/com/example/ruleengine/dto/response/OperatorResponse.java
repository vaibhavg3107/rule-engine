package com.example.ruleengine.dto.response;

import com.example.ruleengine.entity.enums.OperandType;
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
public class OperatorResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<String> compatibleFeatureTypes;
    private OperandType operandType;
    private String operandElementType;
}
