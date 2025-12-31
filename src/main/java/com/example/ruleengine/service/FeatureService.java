package com.example.ruleengine.service;

import com.example.ruleengine.dto.request.CreateFeatureRequest;
import com.example.ruleengine.dto.request.UpdateFeatureRequest;
import com.example.ruleengine.dto.response.FeatureResponse;
import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeatureService {

    private final FeatureRepository featureRepository;

    @Transactional
    public FeatureResponse createFeature(CreateFeatureRequest request) {
        if (featureRepository.existsByName(request.getName())) {
            throw new ValidationException("Feature with name '" + request.getName() + "' already exists");
        }

        Feature feature = Feature.builder()
                .name(request.getName())
                .description(request.getDescription())
                .featureType(request.getFeatureType())
                .extractionConfig(request.getExtractionConfig())
                .defaultValue(request.getDefaultValue())
                .build();

        Feature savedFeature = featureRepository.save(feature);
        return mapToResponse(savedFeature);
    }

    public FeatureResponse getFeatureById(UUID id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", id));
        return mapToResponse(feature);
    }

    public FeatureResponse getFeatureByName(String name) {
        Feature feature = featureRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "name", name));
        return mapToResponse(feature);
    }

    public List<FeatureResponse> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FeatureResponse> getFeaturesByType(FeatureType featureType) {
        return featureRepository.findByFeatureType(featureType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeatureResponse updateFeature(UUID id, UpdateFeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", id));

        if (request.getName() != null && !request.getName().equals(feature.getName())) {
            if (featureRepository.existsByName(request.getName())) {
                throw new ValidationException("Feature with name '" + request.getName() + "' already exists");
            }
            feature.setName(request.getName());
        }

        if (request.getDescription() != null) {
            feature.setDescription(request.getDescription());
        }

        if (request.getFeatureType() != null) {
            feature.setFeatureType(request.getFeatureType());
        }

        if (request.getExtractionConfig() != null) {
            feature.setExtractionConfig(request.getExtractionConfig());
        }

        if (request.getDefaultValue() != null) {
            feature.setDefaultValue(request.getDefaultValue());
        }

        feature.setVersion(feature.getVersion() + 1);
        Feature updatedFeature = featureRepository.save(feature);
        return mapToResponse(updatedFeature);
    }

    @Transactional
    public void deleteFeature(UUID id) {
        if (!featureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Feature", "id", id);
        }
        featureRepository.deleteById(id);
    }

    private FeatureResponse mapToResponse(Feature feature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .name(feature.getName())
                .description(feature.getDescription())
                .featureType(feature.getFeatureType())
                .extractionConfig(feature.getExtractionConfig())
                .defaultValue(feature.getDefaultValue())
                .version(feature.getVersion())
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .build();
    }
}
