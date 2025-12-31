package com.example.ruleengine.repository;

import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.enums.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    
    Optional<Policy> findByName(String name);
    
    List<Policy> findByPolicyType(PolicyType policyType);
    
    List<Policy> findByEnabled(Boolean enabled);
    
    boolean existsByName(String name);
}
