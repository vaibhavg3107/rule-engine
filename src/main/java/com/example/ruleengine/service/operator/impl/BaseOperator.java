package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.service.operator.OperatorStrategy;

import java.util.List;

public abstract class BaseOperator implements OperatorStrategy {

    @SuppressWarnings("unchecked")
    protected int compareValues(Object value, Object operand) {
        if (value instanceof Number && operand instanceof Number) {
            double v1 = ((Number) value).doubleValue();
            double v2 = ((Number) operand).doubleValue();
            return Double.compare(v1, v2);
        }
        if (value instanceof Comparable && operand instanceof Comparable) {
            return ((Comparable<Object>) value).compareTo(operand);
        }
        throw new ValidationException("Cannot compare values: " + value + " and " + operand);
    }

    protected boolean evaluateEquals(Object value, Object operand) {
        if (value instanceof Number && operand instanceof Number) {
            return ((Number) value).doubleValue() == ((Number) operand).doubleValue();
        }
        return value.equals(operand);
    }

    protected int getSize(Object value) {
        if (value instanceof List) {
            return ((List<?>) value).size();
        }
        if (value instanceof String) {
            return ((String) value).length();
        }
        throw new ValidationException("SIZE operators require list or string value");
    }

    protected int getNumericOperand(Object operand) {
        if (operand instanceof Number) {
            return ((Number) operand).intValue();
        }
        throw new ValidationException("SIZE operators require numeric operand");
    }
}
