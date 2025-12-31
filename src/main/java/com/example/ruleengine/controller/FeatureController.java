package com.example.ruleengine.controller;

import com.example.ruleengine.dto.request.CreateFeatureRequest;
import com.example.ruleengine.dto.request.UpdateFeatureRequest;
import com.example.ruleengine.dto.response.FeatureResponse;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.service.FeatureService;
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
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
@Tag(name = "Features", description = "Feature management APIs")
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping
    @Operation(summary = "Create a new feature")
    public ResponseEntity<FeatureResponse> createFeature(@Valid @RequestBody CreateFeatureRequest request) {
        FeatureResponse response = featureService.createFeature(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID")
    public ResponseEntity<FeatureResponse> getFeatureById(@PathVariable UUID id) {
        FeatureResponse response = featureService.getFeatureById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all features or filter by type")
    public ResponseEntity<List<FeatureResponse>> getFeatures(
            @RequestParam(required = false) FeatureType featureType) {
        List<FeatureResponse> response;
        if (featureType != null) {
            response = featureService.getFeaturesByType(featureType);
        } else {
            response = featureService.getAllFeatures();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a feature")
    public ResponseEntity<FeatureResponse> updateFeature(
            @PathVariable UUID id,
            @RequestBody UpdateFeatureRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a feature")
    public ResponseEntity<Void> deleteFeature(@PathVariable UUID id) {
        featureService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }
}
