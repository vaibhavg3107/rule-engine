package com.example.ruleengine.repository;

import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.enums.FeatureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, UUID> {
    
    Optional<Feature> findByName(String name);
    
    List<Feature> findByFeatureType(FeatureType featureType);
    
    boolean existsByName(String name);
}
