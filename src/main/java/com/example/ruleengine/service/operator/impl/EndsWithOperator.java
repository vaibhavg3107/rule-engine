package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class EndsWithOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "ENDS_WITH";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        if (!(featureValue instanceof String)) {
            throw new ValidationException("ENDS_WITH requires string value");
        }
        return ((String) featureValue).endsWith(operand.toString());
    }
}
