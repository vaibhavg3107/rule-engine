package com.example.ruleengine.service;

import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.Operator;
import com.example.ruleengine.entity.Rule;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.entity.enums.OperandType;
import com.example.ruleengine.service.operator.OperatorStrategy;
import com.example.ruleengine.service.operator.OperatorStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluationServiceTest {

    private RuleEvaluationService ruleEvaluationService;

    @BeforeEach
    void setUp() {
        List<OperatorStrategy> strategies = Arrays.asList(
                new com.example.ruleengine.service.operator.impl.EqualsOperator(),
                new com.example.ruleengine.service.operator.impl.NotEqualsOperator(),
                new com.example.ruleengine.service.operator.impl.LessThanOperator(),
                new com.example.ruleengine.service.operator.impl.LessThanOrEqualOperator(),
                new com.example.ruleengine.service.operator.impl.GreaterThanOperator(),
                new com.example.ruleengine.service.operator.impl.GreaterThanOrEqualOperator(),
                new com.example.ruleengine.service.operator.impl.InOperator(),
                new com.example.ruleengine.service.operator.impl.NotInOperator(),
                new com.example.ruleengine.service.operator.impl.BetweenOperator(),
                new com.example.ruleengine.service.operator.impl.ContainsAllOperator(),
                new com.example.ruleengine.service.operator.impl.ContainsAnyOperator(),
                new com.example.ruleengine.service.operator.impl.ContainsOperator(),
                new com.example.ruleengine.service.operator.impl.StartsWithOperator(),
                new com.example.ruleengine.service.operator.impl.EndsWithOperator(),
                new com.example.ruleengine.service.operator.impl.RegexOperator(),
                new com.example.ruleengine.service.operator.impl.IsEmptyOperator(),
                new com.example.ruleengine.service.operator.impl.IsNotEmptyOperator(),
                new com.example.ruleengine.service.operator.impl.SizeEqualsOperator(),
                new com.example.ruleengine.service.operator.impl.SizeGreaterThanOperator(),
                new com.example.ruleengine.service.operator.impl.SizeLessThanOperator()
        );
        OperatorStrategyFactory factory = new OperatorStrategyFactory(strategies);
        factory.init();
        ruleEvaluationService = new RuleEvaluationService(factory);
    }

    private Rule createRule(String operatorCode, OperandType operandType, Object operand) {
        Operator operator = Operator.builder()
                .id(UUID.randomUUID())
                .code(operatorCode)
                .name(operatorCode)
                .operandType(operandType)
                .build();

        Feature feature = Feature.builder()
                .id(UUID.randomUUID())
                .name("test_feature")
                .featureType(FeatureType.NUMERIC)
                .build();

        return Rule.builder()
                .id(UUID.randomUUID())
                .name("test_rule")
                .feature(feature)
                .operator(operator)
                .operand(operand)
                .build();
    }

    @Nested
    @DisplayName("Equality Operators")
    class EqualityOperators {

        @Test
        @DisplayName("EQ - should return true when values are equal")
        void testEqualsTrue() {
            Rule rule = createRule("EQ", OperandType.SINGLE, 100);
            assertTrue(ruleEvaluationService.evaluateRule(rule, 100));
        }

        @Test
        @DisplayName("EQ - should return false when values are not equal")
        void testEqualsFalse() {
            Rule rule = createRule("EQ", OperandType.SINGLE, 100);
            assertFalse(ruleEvaluationService.evaluateRule(rule, 50));
        }

        @Test
        @DisplayName("NEQ - should return true when values are not equal")
        void testNotEqualsTrue() {
            Rule rule = createRule("NEQ", OperandType.SINGLE, 100);
            assertTrue(ruleEvaluationService.evaluateRule(rule, 50));
        }

        @Test
        @DisplayName("NEQ - should return false when values are equal")
        void testNotEqualsFalse() {
            Rule rule = createRule("NEQ", OperandType.SINGLE, 100);
            assertFalse(ruleEvaluationService.evaluateRule(rule, 100));
        }
    }

    @Nested
    @DisplayName("Comparison Operators")
    class ComparisonOperators {

        @Test
        @DisplayName("LT - should return true when value is less than operand")
        void testLessThanTrue() {
            Rule rule = createRule("LT", OperandType.SINGLE, 100);
            assertTrue(ruleEvaluationService.evaluateRule(rule, 50));
        }

        @Test
        @DisplayName("LT - should return false when value is greater or equal")
        void testLessThanFalse() {
            Rule rule = createRule("LT", OperandType.SINGLE, 100);
            assertFalse(ruleEvaluationService.evaluateRule(rule, 100));
            assertFalse(ruleEvaluationService.evaluateRule(rule, 150));
        }

        @Test
        @DisplayName("LTE - should return true when value is less than or equal")
        void testLessThanOrEqualTrue() {
            Rule rule = createRule("LTE", OperandType.SINGLE, 100);
            assertTrue(ruleEvaluationService.evaluateRule(rule, 50));
            assertTrue(ruleEvaluationService.evaluateRule(rule, 100));
        }

        @Test
        @DisplayName("GT - should return true when value is greater than operand")
        void testGreaterThanTrue() {
            Rule rule = createRule("GT", OperandType.SINGLE, 100);
            assertTrue(ruleEvaluationService.evaluateRule(rule, 150));
        }

        @Test
        @DisplayName("GT - should return false when value is less or equal")
        void testGreaterThanFalse() {
            Rule rule = createRule("GT", OperandType.SINGLE, 100);
            assertFalse(ruleEvaluationService.evaluateRule(rule, 100));
            assertFalse(ruleEvaluationService.evaluateRule(rule, 50));
        }

        @Test
        @DisplayName("GTE - should return true when value is greater than or equal")
        void testGreaterThanOrEqualTrue() {
            Rule rule = createRule("GTE", OperandType.SINGLE, 100);
            assertTrue(ruleEvaluationService.evaluateRule(rule, 150));
            assertTrue(ruleEvaluationService.evaluateRule(rule, 100));
        }
    }

    @Nested
    @DisplayName("List Operators")
    class ListOperators {

        @Test
        @DisplayName("IN - should return true when value is in list")
        void testInListTrue() {
            Rule rule = createRule("IN", OperandType.LIST, Arrays.asList(10, 20, 30));
            assertTrue(ruleEvaluationService.evaluateRule(rule, 20));
        }

        @Test
        @DisplayName("IN - should return false when value is not in list")
        void testInListFalse() {
            Rule rule = createRule("IN", OperandType.LIST, Arrays.asList(10, 20, 30));
            assertFalse(ruleEvaluationService.evaluateRule(rule, 40));
        }

        @Test
        @DisplayName("NOT_IN - should return true when value is not in list")
        void testNotInListTrue() {
            Rule rule = createRule("NOT_IN", OperandType.LIST, Arrays.asList(10, 20, 30));
            assertTrue(ruleEvaluationService.evaluateRule(rule, 40));
        }

        @Test
        @DisplayName("NOT_IN - should return false when value is in list")
        void testNotInListFalse() {
            Rule rule = createRule("NOT_IN", OperandType.LIST, Arrays.asList(10, 20, 30));
            assertFalse(ruleEvaluationService.evaluateRule(rule, 20));
        }
    }

    @Nested
    @DisplayName("Range Operator")
    class RangeOperator {

        @Test
        @DisplayName("BETWEEN - should return true when value is within range")
        void testBetweenTrue() {
            Map<String, Object> range = new HashMap<>();
            range.put("min", 10);
            range.put("max", 100);
            Rule rule = createRule("BETWEEN", OperandType.RANGE, range);
            
            assertTrue(ruleEvaluationService.evaluateRule(rule, 50));
            assertTrue(ruleEvaluationService.evaluateRule(rule, 10));
            assertTrue(ruleEvaluationService.evaluateRule(rule, 100));
        }

        @Test
        @DisplayName("BETWEEN - should return false when value is outside range")
        void testBetweenFalse() {
            Map<String, Object> range = new HashMap<>();
            range.put("min", 10);
            range.put("max", 100);
            Rule rule = createRule("BETWEEN", OperandType.RANGE, range);
            
            assertFalse(ruleEvaluationService.evaluateRule(rule, 5));
            assertFalse(ruleEvaluationService.evaluateRule(rule, 150));
        }
    }

    @Nested
    @DisplayName("String Operators")
    class StringOperators {

        @Test
        @DisplayName("CONTAINS - should return true when string contains substring")
        void testContainsTrue() {
            Rule rule = createRule("CONTAINS", OperandType.SINGLE, "world");
            assertTrue(ruleEvaluationService.evaluateRule(rule, "hello world"));
        }

        @Test
        @DisplayName("CONTAINS - should return false when string does not contain substring")
        void testContainsFalse() {
            Rule rule = createRule("CONTAINS", OperandType.SINGLE, "xyz");
            assertFalse(ruleEvaluationService.evaluateRule(rule, "hello world"));
        }

        @Test
        @DisplayName("STARTS_WITH - should return true when string starts with prefix")
        void testStartsWithTrue() {
            Rule rule = createRule("STARTS_WITH", OperandType.SINGLE, "hello");
            assertTrue(ruleEvaluationService.evaluateRule(rule, "hello world"));
        }

        @Test
        @DisplayName("STARTS_WITH - should return false when string does not start with prefix")
        void testStartsWithFalse() {
            Rule rule = createRule("STARTS_WITH", OperandType.SINGLE, "world");
            assertFalse(ruleEvaluationService.evaluateRule(rule, "hello world"));
        }

        @Test
        @DisplayName("ENDS_WITH - should return true when string ends with suffix")
        void testEndsWithTrue() {
            Rule rule = createRule("ENDS_WITH", OperandType.SINGLE, "world");
            assertTrue(ruleEvaluationService.evaluateRule(rule, "hello world"));
        }

        @Test
        @DisplayName("REGEX - should return true when string matches pattern")
        void testRegexTrue() {
            Rule rule = createRule("REGEX", OperandType.SINGLE, "^[a-z]+@[a-z]+\\.[a-z]+$");
            assertTrue(ruleEvaluationService.evaluateRule(rule, "test@example.com"));
        }

        @Test
        @DisplayName("REGEX - should return false when string does not match pattern")
        void testRegexFalse() {
            Rule rule = createRule("REGEX", OperandType.SINGLE, "^[a-z]+@[a-z]+\\.[a-z]+$");
            assertFalse(ruleEvaluationService.evaluateRule(rule, "invalid-email"));
        }
    }

    @Nested
    @DisplayName("List-specific Operators")
    class ListSpecificOperators {

        @Test
        @DisplayName("CONTAINS_ALL - should return true when list contains all values")
        void testContainsAllTrue() {
            Rule rule = createRule("CONTAINS_ALL", OperandType.LIST, Arrays.asList("a", "b"));
            assertTrue(ruleEvaluationService.evaluateRule(rule, Arrays.asList("a", "b", "c")));
        }

        @Test
        @DisplayName("CONTAINS_ALL - should return false when list does not contain all values")
        void testContainsAllFalse() {
            Rule rule = createRule("CONTAINS_ALL", OperandType.LIST, Arrays.asList("a", "b", "d"));
            assertFalse(ruleEvaluationService.evaluateRule(rule, Arrays.asList("a", "b", "c")));
        }

        @Test
        @DisplayName("CONTAINS_ANY - should return true when list contains any value")
        void testContainsAnyTrue() {
            Rule rule = createRule("CONTAINS_ANY", OperandType.LIST, Arrays.asList("x", "y", "a"));
            assertTrue(ruleEvaluationService.evaluateRule(rule, Arrays.asList("a", "b", "c")));
        }

        @Test
        @DisplayName("CONTAINS_ANY - should return false when list contains none of the values")
        void testContainsAnyFalse() {
            Rule rule = createRule("CONTAINS_ANY", OperandType.LIST, Arrays.asList("x", "y", "z"));
            assertFalse(ruleEvaluationService.evaluateRule(rule, Arrays.asList("a", "b", "c")));
        }
    }

    @Nested
    @DisplayName("Empty Check Operators")
    class EmptyCheckOperators {

        @Test
        @DisplayName("IS_EMPTY - should return true for null value")
        void testIsEmptyNull() {
            Rule rule = createRule("IS_EMPTY", OperandType.NONE, null);
            assertTrue(ruleEvaluationService.evaluateRule(rule, null));
        }

        @Test
        @DisplayName("IS_EMPTY - should return true for empty string")
        void testIsEmptyString() {
            Rule rule = createRule("IS_EMPTY", OperandType.NONE, null);
            assertTrue(ruleEvaluationService.evaluateRule(rule, ""));
        }

        @Test
        @DisplayName("IS_EMPTY - should return true for empty list")
        void testIsEmptyList() {
            Rule rule = createRule("IS_EMPTY", OperandType.NONE, null);
            assertTrue(ruleEvaluationService.evaluateRule(rule, new ArrayList<>()));
        }

        @Test
        @DisplayName("IS_NOT_EMPTY - should return true for non-empty value")
        void testIsNotEmptyTrue() {
            Rule rule = createRule("IS_NOT_EMPTY", OperandType.NONE, null);
            assertTrue(ruleEvaluationService.evaluateRule(rule, "hello"));
            assertTrue(ruleEvaluationService.evaluateRule(rule, Arrays.asList(1, 2, 3)));
        }
    }

    @Nested
    @DisplayName("Size Operators")
    class SizeOperators {

        @Test
        @DisplayName("SIZE_EQ - should return true when list size equals operand")
        void testSizeEqualsTrue() {
            Rule rule = createRule("SIZE_EQ", OperandType.SINGLE, 3);
            assertTrue(ruleEvaluationService.evaluateRule(rule, Arrays.asList(1, 2, 3)));
        }

        @Test
        @DisplayName("SIZE_GT - should return true when list size is greater than operand")
        void testSizeGreaterThanTrue() {
            Rule rule = createRule("SIZE_GT", OperandType.SINGLE, 2);
            assertTrue(ruleEvaluationService.evaluateRule(rule, Arrays.asList(1, 2, 3)));
        }

        @Test
        @DisplayName("SIZE_LT - should return true when list size is less than operand")
        void testSizeLessThanTrue() {
            Rule rule = createRule("SIZE_LT", OperandType.SINGLE, 5);
            assertTrue(ruleEvaluationService.evaluateRule(rule, Arrays.asList(1, 2, 3)));
        }
    }
}
