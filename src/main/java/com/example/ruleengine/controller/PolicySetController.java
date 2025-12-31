package com.example.ruleengine.controller;

import com.example.ruleengine.dto.request.CreatePolicySetRequest;
import com.example.ruleengine.dto.request.TestRuleRequest;
import com.example.ruleengine.dto.response.PolicySetResponse;
import com.example.ruleengine.dto.response.UnifiedEvaluationResultResponse;
import com.example.ruleengine.service.PolicySetService;
import com.example.ruleengine.service.UnifiedEvaluationService;
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
@RequestMapping("/api/v1/policy-sets")
@RequiredArgsConstructor
@Tag(name = "PolicySets", description = "PolicySet management and unified evaluation APIs")
public class PolicySetController {

    private final PolicySetService policySetService;
    private final UnifiedEvaluationService unifiedEvaluationService;

    @PostMapping
    @Operation(summary = "Create a new policy set")
    public ResponseEntity<PolicySetResponse> createPolicySet(@Valid @RequestBody CreatePolicySetRequest request) {
        PolicySetResponse response = policySetService.createPolicySet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy set by ID")
    public ResponseEntity<PolicySetResponse> getPolicySetById(@PathVariable UUID id) {
        PolicySetResponse response = policySetService.getPolicySetById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all policy sets")
    public ResponseEntity<List<PolicySetResponse>> getAllPolicySets(
            @RequestParam(required = false) Boolean enabled) {
        List<PolicySetResponse> response;
        if (Boolean.TRUE.equals(enabled)) {
            response = policySetService.getEnabledPolicySets();
        } else {
            response = policySetService.getAllPolicySets();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a policy set")
    public ResponseEntity<PolicySetResponse> updatePolicySet(
            @PathVariable UUID id,
            @RequestBody CreatePolicySetRequest request) {
        PolicySetResponse response = policySetService.updatePolicySet(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a policy set")
    public ResponseEntity<Void> deletePolicySet(@PathVariable UUID id) {
        policySetService.deletePolicySet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluate a policy set (unified evaluation combining boolean and offer policies)")
    public ResponseEntity<UnifiedEvaluationResultResponse> evaluatePolicySet(
            @PathVariable UUID id,
            @Valid @RequestBody TestRuleRequest request) {
        UnifiedEvaluationResultResponse result = 
                unifiedEvaluationService.evaluate(id, request.getInputData());
        return ResponseEntity.ok(result);
    }
}
