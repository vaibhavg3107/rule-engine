package com.example.ruleengine.controller;

import com.example.ruleengine.dto.request.CreatePolicyRequest;
import com.example.ruleengine.dto.request.TestRuleRequest;
import com.example.ruleengine.dto.response.PolicyResponse;
import com.example.ruleengine.entity.enums.PolicyType;
import com.example.ruleengine.service.PolicyEvaluationService;
import com.example.ruleengine.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Policy management APIs")
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyEvaluationService policyEvaluationService;

    @PostMapping
    @Operation(summary = "Create a new policy")
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable UUID id) {
        PolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all policies or filter by type")
    public ResponseEntity<List<PolicyResponse>> getPolicies(
            @RequestParam(required = false) PolicyType policyType) {
        List<PolicyResponse> response;
        if (policyType != null) {
            response = policyService.getPoliciesByType(policyType);
        } else {
            response = policyService.getAllPolicies();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a policy")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable UUID id,
            @RequestBody CreatePolicyRequest request) {
        PolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a policy")
    public ResponseEntity<Void> deletePolicy(@PathVariable UUID id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluate a policy against input data")
    public ResponseEntity<PolicyEvaluationService.PolicyEvaluationResult> evaluatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody TestRuleRequest request) {
        PolicyEvaluationService.PolicyEvaluationResult result = 
                policyEvaluationService.evaluatePolicy(id, request.getInputData());
        return ResponseEntity.ok(result);
    }
}
