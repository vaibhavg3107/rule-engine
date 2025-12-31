package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RegexOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "REGEX";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        if (!(featureValue instanceof String)) {
            throw new ValidationException("REGEX requires string value");
        }
        Pattern pattern = Pattern.compile(operand.toString());
        return pattern.matcher((String) featureValue).matches();
    }
}
