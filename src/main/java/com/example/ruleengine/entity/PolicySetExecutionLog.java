package com.example.ruleengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "policy_set_execution_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicySetExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_set_id")
    private UUID policySetId;

    @Column(name = "policy_set_version")
    private Integer policySetVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data", columnDefinition = "jsonb")
    private Map<String, Object> inputData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_features", columnDefinition = "jsonb")
    private Map<String, Object> extractedFeatures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "boolean_policy_result", columnDefinition = "jsonb")
    private Map<String, Object> booleanPolicyResult;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "offer_policy_result", columnDefinition = "jsonb")
    private Map<String, Object> offerPolicyResult;

    @Column(name = "decision_status")
    private String decisionStatus;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
