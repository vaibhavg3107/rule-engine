package com.example.ruleengine.repository;

import com.example.ruleengine.entity.PolicySetExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PolicySetExecutionLogRepository extends JpaRepository<PolicySetExecutionLog, UUID> {

    List<PolicySetExecutionLog> findByPolicySetIdOrderByExecutedAtDesc(UUID policySetId);

    List<PolicySetExecutionLog> findByPolicySetIdAndExecutedAtBetweenOrderByExecutedAtDesc(
            UUID policySetId, LocalDateTime startTime, LocalDateTime endTime);

    List<PolicySetExecutionLog> findByDecisionStatusOrderByExecutedAtDesc(String decisionStatus);

    List<PolicySetExecutionLog> findTop100ByOrderByExecutedAtDesc();
}
