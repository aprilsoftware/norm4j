package org.norm4j;

public abstract class Expression
{
    public abstract String build();

    public static CompareExpression compare()
    {
        return new CompareExpression();
    }

    public static class CompareExpression extends Expression
    {
        private Expression leftOperand;
        private String operator;
        private Expression rightOperand;

        public CompareExpression()
        {
        }

        public CompareExpression leftOperand(Expression leftOperand)
        {
            this.leftOperand = leftOperand;

            return this;
        }

        public CompareExpression operator(String operator)
        {
            this.operator = operator;

            return this;
        }

        public CompareExpression rightOperand(Expression rightOperand)
        {
            this.rightOperand = rightOperand;

            return this;
        }

        public String build()
        {
            StringBuilder expression;

            if (leftOperand == null)
            {
                throw new RuntimeException("Missing left operand.");
            }

            if (operator == null)
            {
                throw new RuntimeException("Missing operator.");
            }

            if (rightOperand == null)
            {
                throw new RuntimeException("Missing right operand.");
            }

            expression = new StringBuilder();

            expression.append(leftOperand.build());

            expression.append(operator);

            expression.append(rightOperand.build());

            return expression.toString();
        }
    }
}
