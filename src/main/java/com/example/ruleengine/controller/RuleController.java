package com.example.ruleengine.controller;

import com.example.ruleengine.dto.request.CreateRuleRequest;
import com.example.ruleengine.dto.request.TestRuleRequest;
import com.example.ruleengine.dto.response.RuleResponse;
import com.example.ruleengine.dto.response.TestRuleResponse;
import com.example.ruleengine.service.RuleService;
import com.example.ruleengine.service.RuleTestService;
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
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Rules", description = "Rule management APIs")
public class RuleController {

    private final RuleService ruleService;
    private final RuleTestService ruleTestService;

    @PostMapping
    @Operation(summary = "Create a new rule")
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody CreateRuleRequest request) {
        RuleResponse response = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rule by ID")
    public ResponseEntity<RuleResponse> getRuleById(@PathVariable UUID id) {
        RuleResponse response = ruleService.getRuleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all rules or filter by feature ID")
    public ResponseEntity<List<RuleResponse>> getRules(
            @RequestParam(required = false) UUID featureId) {
        List<RuleResponse> response;
        if (featureId != null) {
            response = ruleService.getRulesByFeatureId(featureId);
        } else {
            response = ruleService.getAllRules();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a rule")
    public ResponseEntity<RuleResponse> updateRule(
            @PathVariable UUID id,
            @RequestBody CreateRuleRequest request) {
        RuleResponse response = ruleService.updateRule(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rule")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test a rule against input data")
    public ResponseEntity<TestRuleResponse> testRule(
            @PathVariable UUID id,
            @Valid @RequestBody TestRuleRequest request) {
        TestRuleResponse response = ruleTestService.testRule(id, request.getInputData());
        return ResponseEntity.ok(response);
    }
}
