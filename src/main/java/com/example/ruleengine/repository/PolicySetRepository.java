package com.example.ruleengine.repository;

import com.example.ruleengine.entity.PolicySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicySetRepository extends JpaRepository<PolicySet, UUID> {
    
    Optional<PolicySet> findByName(String name);
    
    List<PolicySet> findByEnabled(Boolean enabled);
    
    boolean existsByName(String name);
}
