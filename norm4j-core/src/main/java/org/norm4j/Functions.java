package org.norm4j;

import java.util.List;

public class Functions
{
    public Functions()
    {
    }

    public static <T, R> Expression coalesce(final Object... values)
    {
        return new Expression()
        {
            public String build(TableManager tableManager, 
                    List<Object> parameters)
            {
                StringBuilder expression;
                boolean onlyNull = true;

                expression = new StringBuilder();

                for (Object value : values)
                {
                    if (value != null)
                    {
                        onlyNull = false;

                        break;
                    }
                }

                if (onlyNull)
                {
                    expression.append("NULL");
                }
                else
                {
                    expression.append("COALESCE(");

                    for (int i = 0; i < values.length; i++)
                    {
                        Object value;
    
                        if (i > 0)
                        {
                            expression.append(", ");
                        }
    
                        value = values[i];
    
                        if (value == null)
                        {
                            expression.append("NULL");
                        }
                        else
                        {
                            expression.append("?");
    
                            parameters.add(value);
                        }
                    }
    
                    expression.append(", NULL)");
                }

                return expression.toString();
            }
        };
    }
}
