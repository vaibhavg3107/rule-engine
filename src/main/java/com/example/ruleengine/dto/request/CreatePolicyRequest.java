package com.example.ruleengine.dto.request;

import com.example.ruleengine.entity.enums.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePolicyRequest {

    @NotBlank(message = "Policy name is required")
    private String name;

    private String description;

    @NotNull(message = "Policy type is required")
    private PolicyType policyType;

    @NotNull(message = "Root node is required")
    private Map<String, Object> rootNode;

    private Map<String, Object> outputMapping;
}
