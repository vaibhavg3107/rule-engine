package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class LessThanOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "LT";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return compareValues(featureValue, operand) < 0;
    }
}
