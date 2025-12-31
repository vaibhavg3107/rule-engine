package com.example.ruleengine.repository;

import com.example.ruleengine.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, UUID> {
    
    Optional<Operator> findByCode(String code);
    
    @Query(value = "SELECT * FROM operators WHERE compatible_feature_types @> :featureType::jsonb", nativeQuery = true)
    List<Operator> findByCompatibleFeatureType(@Param("featureType") String featureType);
}
