package com.example.ruleengine.service.operator;

public interface OperatorStrategy {
    
    String getOperatorCode();
    
    boolean evaluate(Object featureValue, Object operand);
    
    default boolean handleNullValue() {
        return false;
    }
}
