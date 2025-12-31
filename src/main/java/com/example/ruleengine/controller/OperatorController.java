package com.example.ruleengine.controller;

import com.example.ruleengine.dto.response.OperatorResponse;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operators")
@RequiredArgsConstructor
@Tag(name = "Operators", description = "Operator query APIs")
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping
    @Operation(summary = "Get all operators or filter by feature type")
    public ResponseEntity<List<OperatorResponse>> getOperators(
            @RequestParam(required = false) FeatureType featureType) {
        List<OperatorResponse> response;
        if (featureType != null) {
            response = operatorService.getOperatorsByFeatureType(featureType);
        } else {
            response = operatorService.getAllOperators();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get operator by code")
    public ResponseEntity<OperatorResponse> getOperatorByCode(@PathVariable String code) {
        OperatorResponse response = operatorService.getOperatorByCode(code);
        return ResponseEntity.ok(response);
    }
}
