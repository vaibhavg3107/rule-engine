package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IsEmptyOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "IS_EMPTY";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        if (featureValue == null) {
            return true;
        }
        if (featureValue instanceof String) {
            return ((String) featureValue).isEmpty();
        }
        if (featureValue instanceof List) {
            return ((List<?>) featureValue).isEmpty();
        }
        return false;
    }

    @Override
    public boolean handleNullValue() {
        return true;
    }
}
