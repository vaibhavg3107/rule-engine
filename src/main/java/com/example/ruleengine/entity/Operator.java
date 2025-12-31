package com.example.ruleengine.entity;

import com.example.ruleengine.entity.enums.OperandType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "operators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operator {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Type(JsonType.class)
    @Column(name = "compatible_feature_types", columnDefinition = "jsonb")
    private List<String> compatibleFeatureTypes;

    @Enumerated(EnumType.STRING)
    @Column(name = "operand_type", nullable = false)
    private OperandType operandType;

    @Column(name = "operand_element_type")
    private String operandElementType;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
