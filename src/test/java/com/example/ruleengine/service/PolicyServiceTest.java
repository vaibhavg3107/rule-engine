package com.example.ruleengine.service;

import com.example.ruleengine.dto.request.CreatePolicyRequest;
import com.example.ruleengine.dto.response.PolicyResponse;
import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.enums.PolicyType;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.PolicyRepository;
import com.example.ruleengine.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private PolicyService policyService;

    private CreatePolicyRequest validRequest;
    private Policy savedPolicy;
    private UUID policyId;
    private UUID ruleId;

    @BeforeEach
    void setUp() {
        policyId = UUID.randomUUID();
        ruleId = UUID.randomUUID();

        Map<String, Object> rootNode = new HashMap<>();
        rootNode.put("type", "LEAF");
        rootNode.put("ruleId", ruleId.toString());

        validRequest = CreatePolicyRequest.builder()
                .name("test_policy")
                .description("Test policy description")
                .policyType(PolicyType.BOOLEAN)
                .rootNode(rootNode)
                .build();

        savedPolicy = Policy.builder()
                .id(policyId)
                .name("test_policy")
                .description("Test policy description")
                .policyType(PolicyType.BOOLEAN)
                .rootNode(rootNode)
                .enabled(true)
                .version(1)
                .build();
    }

    @Nested
    @DisplayName("Create Policy Tests")
    class CreatePolicyTests {

        @Test
        @DisplayName("Should create policy successfully")
        void testCreatePolicySuccess() {
            when(policyRepository.existsByName("test_policy")).thenReturn(false);
            when(ruleRepository.existsById(ruleId)).thenReturn(true);
            when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

            PolicyResponse response = policyService.createPolicy(validRequest);

            assertNotNull(response);
            assertEquals("test_policy", response.getName());
            assertEquals(PolicyType.BOOLEAN, response.getPolicyType());
            verify(policyRepository).save(any(Policy.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate name")
        void testCreatePolicyDuplicateName() {
            when(policyRepository.existsByName("test_policy")).thenReturn(true);

            assertThrows(ValidationException.class, () -> policyService.createPolicy(validRequest));
            verify(policyRepository, never()).save(any(Policy.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid rule in tree")
        void testCreatePolicyInvalidRule() {
            when(policyRepository.existsByName("test_policy")).thenReturn(false);
            when(ruleRepository.existsById(ruleId)).thenReturn(false);

            assertThrows(ValidationException.class, () -> policyService.createPolicy(validRequest));
            verify(policyRepository, never()).save(any(Policy.class));
        }
    }

    @Nested
    @DisplayName("Get Policy Tests")
    class GetPolicyTests {

        @Test
        @DisplayName("Should get policy by ID")
        void testGetPolicyById() {
            when(policyRepository.findById(policyId)).thenReturn(Optional.of(savedPolicy));

            PolicyResponse response = policyService.getPolicyById(policyId);

            assertNotNull(response);
            assertEquals(policyId, response.getId());
            assertEquals("test_policy", response.getName());
        }

        @Test
        @DisplayName("Should throw exception for non-existent policy")
        void testGetPolicyByIdNotFound() {
            when(policyRepository.findById(policyId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> policyService.getPolicyById(policyId));
        }

        @Test
        @DisplayName("Should get all policies")
        void testGetAllPolicies() {
            when(policyRepository.findAll()).thenReturn(List.of(savedPolicy));

            List<PolicyResponse> responses = policyService.getAllPolicies();

            assertEquals(1, responses.size());
            assertEquals("test_policy", responses.get(0).getName());
        }

        @Test
        @DisplayName("Should get policies by type")
        void testGetPoliciesByType() {
            when(policyRepository.findByPolicyType(PolicyType.BOOLEAN)).thenReturn(List.of(savedPolicy));

            List<PolicyResponse> responses = policyService.getPoliciesByType(PolicyType.BOOLEAN);

            assertEquals(1, responses.size());
            assertEquals(PolicyType.BOOLEAN, responses.get(0).getPolicyType());
        }
    }

    @Nested
    @DisplayName("Delete Policy Tests")
    class DeletePolicyTests {

        @Test
        @DisplayName("Should delete policy successfully")
        void testDeletePolicy() {
            when(policyRepository.existsById(policyId)).thenReturn(true);
            doNothing().when(policyRepository).deleteById(policyId);

            assertDoesNotThrow(() -> policyService.deletePolicy(policyId));
            verify(policyRepository).deleteById(policyId);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent policy")
        void testDeletePolicyNotFound() {
            when(policyRepository.existsById(policyId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> policyService.deletePolicy(policyId));
            verify(policyRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Policy Tree Validation Tests")
    class PolicyTreeValidationTests {

        @Test
        @DisplayName("Should validate COMPOSITE node with AND operator")
        void testValidateCompositeAndNode() {
            Map<String, Object> rootNode = new HashMap<>();
            rootNode.put("type", "COMPOSITE");
            rootNode.put("operator", "AND");
            
            List<Map<String, Object>> children = new ArrayList<>();
            Map<String, Object> child1 = new HashMap<>();
            child1.put("type", "LEAF");
            child1.put("ruleId", ruleId.toString());
            children.add(child1);
            
            Map<String, Object> child2 = new HashMap<>();
            child2.put("type", "LEAF");
            child2.put("ruleId", ruleId.toString());
            children.add(child2);
            
            rootNode.put("children", children);

            CreatePolicyRequest request = CreatePolicyRequest.builder()
                    .name("composite_policy")
                    .policyType(PolicyType.BOOLEAN)
                    .rootNode(rootNode)
                    .build();

            when(policyRepository.existsByName("composite_policy")).thenReturn(false);
            when(ruleRepository.existsById(ruleId)).thenReturn(true);
            when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

            assertDoesNotThrow(() -> policyService.createPolicy(request));
        }

        @Test
        @DisplayName("Should validate COMPOSITE node with OR operator")
        void testValidateCompositeOrNode() {
            Map<String, Object> rootNode = new HashMap<>();
            rootNode.put("type", "COMPOSITE");
            rootNode.put("operator", "OR");
            
            List<Map<String, Object>> children = new ArrayList<>();
            Map<String, Object> child = new HashMap<>();
            child.put("type", "LEAF");
            child.put("ruleId", ruleId.toString());
            children.add(child);
            
            rootNode.put("children", children);

            CreatePolicyRequest request = CreatePolicyRequest.builder()
                    .name("or_policy")
                    .policyType(PolicyType.BOOLEAN)
                    .rootNode(rootNode)
                    .build();

            when(policyRepository.existsByName("or_policy")).thenReturn(false);
            when(ruleRepository.existsById(ruleId)).thenReturn(true);
            when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

            assertDoesNotThrow(() -> policyService.createPolicy(request));
        }
    }
}
