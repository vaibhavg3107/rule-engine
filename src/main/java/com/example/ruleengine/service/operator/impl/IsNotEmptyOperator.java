package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IsNotEmptyOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "IS_NOT_EMPTY";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        if (featureValue == null) {
            return false;
        }
        if (featureValue instanceof String) {
            return !((String) featureValue).isEmpty();
        }
        if (featureValue instanceof List) {
            return !((List<?>) featureValue).isEmpty();
        }
        return true;
    }

    @Override
    public boolean handleNullValue() {
        return false;
    }
}
