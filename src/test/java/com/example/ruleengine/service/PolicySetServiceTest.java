package com.example.ruleengine.service;

import com.example.ruleengine.dto.request.CreatePolicySetRequest;
import com.example.ruleengine.dto.response.PolicySetResponse;
import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.PolicySet;
import com.example.ruleengine.entity.enums.EvaluationStrategy;
import com.example.ruleengine.entity.enums.PolicyType;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.PolicyRepository;
import com.example.ruleengine.repository.PolicySetRepository;
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
class PolicySetServiceTest {

    @Mock
    private PolicySetRepository policySetRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicySetService policySetService;

    private UUID policySetId;
    private UUID booleanPolicyId;
    private UUID offerPolicyId;
    private Policy booleanPolicy;
    private Policy offerPolicy;
    private PolicySet savedPolicySet;

    @BeforeEach
    void setUp() {
        policySetId = UUID.randomUUID();
        booleanPolicyId = UUID.randomUUID();
        offerPolicyId = UUID.randomUUID();

        booleanPolicy = Policy.builder()
                .id(booleanPolicyId)
                .name("boolean_policy")
                .policyType(PolicyType.BOOLEAN)
                .rootNode(new HashMap<>())
                .build();

        offerPolicy = Policy.builder()
                .id(offerPolicyId)
                .name("offer_policy")
                .policyType(PolicyType.OFFER)
                .rootNode(new HashMap<>())
                .build();

        savedPolicySet = PolicySet.builder()
                .id(policySetId)
                .name("test_policy_set")
                .description("Test policy set")
                .booleanPolicy(booleanPolicy)
                .evaluationStrategy(EvaluationStrategy.BOOLEAN_FIRST)
                .enabled(true)
                .version(1)
                .build();
    }

    @Nested
    @DisplayName("Create PolicySet Tests")
    class CreatePolicySetTests {

        @Test
        @DisplayName("Should create policy set with both policies")
        void testCreatePolicySetWithBothPolicies() {
            List<CreatePolicySetRequest.OfferPolicyWithPriority> offerPolicies = List.of(
                    CreatePolicySetRequest.OfferPolicyWithPriority.builder()
                            .policyId(offerPolicyId)
                            .priority(1)
                            .enabled(true)
                            .build()
            );

            CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                    .name("test_policy_set")
                    .description("Test policy set")
                    .booleanPolicyId(booleanPolicyId)
                    .offerPolicies(offerPolicies)
                    .evaluationStrategy(EvaluationStrategy.BOOLEAN_FIRST)
                    .build();

            when(policySetRepository.existsByName("test_policy_set")).thenReturn(false);
            when(policyRepository.findById(booleanPolicyId)).thenReturn(Optional.of(booleanPolicy));
            when(policyRepository.findById(offerPolicyId)).thenReturn(Optional.of(offerPolicy));
            when(policySetRepository.save(any(PolicySet.class))).thenReturn(savedPolicySet);

            PolicySetResponse response = policySetService.createPolicySet(request);

            assertNotNull(response);
            assertEquals("test_policy_set", response.getName());
            assertEquals(booleanPolicyId, response.getBooleanPolicyId());
            assertEquals(EvaluationStrategy.BOOLEAN_FIRST, response.getEvaluationStrategy());
        }

        @Test
        @DisplayName("Should create policy set with only boolean policy")
        void testCreatePolicySetWithOnlyBooleanPolicy() {
            CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                    .name("boolean_only_set")
                    .booleanPolicyId(booleanPolicyId)
                    .evaluationStrategy(EvaluationStrategy.BOOLEAN_FIRST)
                    .build();

            PolicySet booleanOnlySet = PolicySet.builder()
                    .id(policySetId)
                    .name("boolean_only_set")
                    .booleanPolicy(booleanPolicy)
                    .evaluationStrategy(EvaluationStrategy.BOOLEAN_FIRST)
                    .build();

            when(policySetRepository.existsByName("boolean_only_set")).thenReturn(false);
            when(policyRepository.findById(booleanPolicyId)).thenReturn(Optional.of(booleanPolicy));
            when(policySetRepository.save(any(PolicySet.class))).thenReturn(booleanOnlySet);

            PolicySetResponse response = policySetService.createPolicySet(request);

            assertNotNull(response);
            assertEquals(booleanPolicyId, response.getBooleanPolicyId());
            assertTrue(response.getOfferPolicies() == null || response.getOfferPolicies().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for duplicate name")
        void testCreatePolicySetDuplicateName() {
            CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                    .name("test_policy_set")
                    .booleanPolicyId(booleanPolicyId)
                    .build();

            when(policySetRepository.existsByName("test_policy_set")).thenReturn(true);

            assertThrows(ValidationException.class, () -> policySetService.createPolicySet(request));
            verify(policySetRepository, never()).save(any(PolicySet.class));
        }

        @Test
        @DisplayName("Should throw exception when boolean policy has wrong type")
        void testCreatePolicySetWrongBooleanPolicyType() {
            Policy wrongTypePolicy = Policy.builder()
                    .id(booleanPolicyId)
                    .name("wrong_type")
                    .policyType(PolicyType.OFFER)
                    .build();

            CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                    .name("test_policy_set")
                    .booleanPolicyId(booleanPolicyId)
                    .build();

            when(policySetRepository.existsByName("test_policy_set")).thenReturn(false);
            when(policyRepository.findById(booleanPolicyId)).thenReturn(Optional.of(wrongTypePolicy));

            assertThrows(ValidationException.class, () -> policySetService.createPolicySet(request));
        }

        @Test
        @DisplayName("Should throw exception when offer policy has wrong type")
        void testCreatePolicySetWrongOfferPolicyType() {
            Policy wrongTypePolicy = Policy.builder()
                    .id(offerPolicyId)
                    .name("wrong_type")
                    .policyType(PolicyType.BOOLEAN)
                    .build();

            List<CreatePolicySetRequest.OfferPolicyWithPriority> offerPolicies = List.of(
                    CreatePolicySetRequest.OfferPolicyWithPriority.builder()
                            .policyId(offerPolicyId)
                            .priority(1)
                            .build()
            );

            CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                    .name("test_policy_set")
                    .booleanPolicyId(booleanPolicyId)
                    .offerPolicies(offerPolicies)
                    .build();

            when(policySetRepository.existsByName("test_policy_set")).thenReturn(false);
            when(policyRepository.findById(booleanPolicyId)).thenReturn(Optional.of(booleanPolicy));
            when(policyRepository.findById(offerPolicyId)).thenReturn(Optional.of(wrongTypePolicy));

            assertThrows(ValidationException.class, () -> policySetService.createPolicySet(request));
        }

        @Test
        @DisplayName("Should throw exception when no policies provided")
        void testCreatePolicySetNoPolicies() {
            CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                    .name("empty_policy_set")
                    .build();

            when(policySetRepository.existsByName("empty_policy_set")).thenReturn(false);

            assertThrows(ValidationException.class, () -> policySetService.createPolicySet(request));
        }
    }

    @Nested
    @DisplayName("Get PolicySet Tests")
    class GetPolicySetTests {

        @Test
        @DisplayName("Should get policy set by ID")
        void testGetPolicySetById() {
            when(policySetRepository.findById(policySetId)).thenReturn(Optional.of(savedPolicySet));

            PolicySetResponse response = policySetService.getPolicySetById(policySetId);

            assertNotNull(response);
            assertEquals(policySetId, response.getId());
            assertEquals("test_policy_set", response.getName());
        }

        @Test
        @DisplayName("Should throw exception for non-existent policy set")
        void testGetPolicySetByIdNotFound() {
            when(policySetRepository.findById(policySetId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> policySetService.getPolicySetById(policySetId));
        }

        @Test
        @DisplayName("Should get all policy sets")
        void testGetAllPolicySets() {
            when(policySetRepository.findAll()).thenReturn(List.of(savedPolicySet));

            List<PolicySetResponse> responses = policySetService.getAllPolicySets();

            assertEquals(1, responses.size());
            assertEquals("test_policy_set", responses.get(0).getName());
        }

        @Test
        @DisplayName("Should get enabled policy sets")
        void testGetEnabledPolicySets() {
            when(policySetRepository.findByEnabled(true)).thenReturn(List.of(savedPolicySet));

            List<PolicySetResponse> responses = policySetService.getEnabledPolicySets();

            assertEquals(1, responses.size());
            assertTrue(responses.get(0).getEnabled());
        }
    }

    @Nested
    @DisplayName("Delete PolicySet Tests")
    class DeletePolicySetTests {

        @Test
        @DisplayName("Should delete policy set successfully")
        void testDeletePolicySet() {
            when(policySetRepository.existsById(policySetId)).thenReturn(true);
            doNothing().when(policySetRepository).deleteById(policySetId);

            assertDoesNotThrow(() -> policySetService.deletePolicySet(policySetId));
            verify(policySetRepository).deleteById(policySetId);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent policy set")
        void testDeletePolicySetNotFound() {
            when(policySetRepository.existsById(policySetId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> policySetService.deletePolicySet(policySetId));
            verify(policySetRepository, never()).deleteById(any());
        }
    }
}
