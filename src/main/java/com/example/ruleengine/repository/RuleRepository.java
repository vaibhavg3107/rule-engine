package com.example.ruleengine.repository;

import com.example.ruleengine.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RuleRepository extends JpaRepository<Rule, UUID> {
    
    List<Rule> findByFeatureId(UUID featureId);
    
    List<Rule> findByEnabled(Boolean enabled);
    
    List<Rule> findByOperatorCode(String operatorCode);
}
