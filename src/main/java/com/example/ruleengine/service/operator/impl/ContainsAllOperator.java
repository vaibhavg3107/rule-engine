package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContainsAllOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "CONTAINS_ALL";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(Object featureValue, Object operand) {
        if (!(featureValue instanceof List) || !(operand instanceof List)) {
            throw new ValidationException("CONTAINS_ALL requires list value and list operand");
        }
        List<Object> valueList = (List<Object>) featureValue;
        List<Object> operandList = (List<Object>) operand;

        for (Object item : operandList) {
            boolean found = false;
            for (Object v : valueList) {
                if (evaluateEquals(v, item)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
