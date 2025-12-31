package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContainsOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "CONTAINS";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(Object featureValue, Object operand) {
        if (featureValue instanceof String) {
            return ((String) featureValue).contains(operand.toString());
        }
        if (featureValue instanceof List) {
            List<Object> list = (List<Object>) featureValue;
            for (Object item : list) {
                if (evaluateEquals(item, operand)) {
                    return true;
                }
            }
            return false;
        }
        throw new ValidationException("CONTAINS requires string or list value");
    }
}
