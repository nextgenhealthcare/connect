package com.truemesh.squiggle;

import com.truemesh.squiggle.output.Output;

/**
 * See OR and AND
 * 
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 */
public abstract class BaseLogicGroup extends Criteria {
    private String operator;
    private Criteria left;
    private Criteria right;

    public BaseLogicGroup(String operator, Criteria left, Criteria right) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public void write(Output out) {
        out.print("( ")
            .print(left)
            .print(' ')
            .print(operator)
            .print(' ')
            .print(right)
            .print(" )");
    }

}
