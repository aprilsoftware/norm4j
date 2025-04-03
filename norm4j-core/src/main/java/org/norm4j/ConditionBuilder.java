package org.norm4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConditionBuilder
{
    private final QueryBuilder queryBuilder;
    private final StringBuilder condition;
    private final List<Object> parameters;

    public ConditionBuilder(QueryBuilder queryBuilder)
    {
        this.queryBuilder = queryBuilder;

        this.condition = new StringBuilder();

        this.parameters = new ArrayList<>();
    }

    protected List<Object> getParameters()
    {
        return parameters;
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return condition(fieldGetter, null, operator, value);
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }
        
        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(value, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder condition(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(value, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<T, R> rightFieldGetter)
    {
        return condition(leftFieldGetter, 
                null, 
                operator, 
                rightFieldGetter, 
                null);
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<T, R> rightFieldGetter,
            String rightAlias)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(leftFieldGetter, 
                leftAlias, 
                operator, 
                rightFieldGetter, 
                rightAlias, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(leftBuilder, 
                operator, 
                rightBuilder, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(builder, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(value, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(builder, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder condition(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(builder, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(fieldGetter, null, operator, builder);
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(Expression expression,
            String operator, 
            Object value)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(expression,
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(String expression)
    {
        return condition(expression, null);
    }

    public ConditionBuilder condition(String expression, List<Object> parameters)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(expression, 
                parameters, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(Consumer<ConditionBuilder> consumer)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(consumer, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return condition(fieldGetter, operator, value);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        return condition(fieldGetter, alias, operator, value);
    }

    public <T, R> ConditionBuilder and(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(value, operator, fieldGetter);
    }

    public <T, R> ConditionBuilder and(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        return condition(value, operator, fieldGetter, alias);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<T, R> rightFieldGetter)
    {
        return condition(leftFieldGetter, 
                operator, 
                rightFieldGetter);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<T, R> rightFieldGetter,
            String rightAlias)
    {
        return condition(leftFieldGetter, leftAlias, operator, rightFieldGetter, rightAlias);
    }

    public ConditionBuilder and(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        return condition(leftBuilder, operator, rightBuilder);
    }

    public ConditionBuilder and(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        return condition(builder, operator, value);
    }

    public ConditionBuilder and(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(value, operator, builder);
    }

    public <T, R> ConditionBuilder and(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(builder, operator, fieldGetter);
    }

    public <T, R> ConditionBuilder and(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        return condition(builder, operator, fieldGetter, alias);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(fieldGetter, operator, builder);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(fieldGetter, alias, operator, builder);
    }

    public ConditionBuilder and(Expression expression,
            String operator, 
            Object value)
    {
        return condition(expression, operator, value);
    }

    public ConditionBuilder and(String expression)
    {
        return condition(expression);
    }

    public ConditionBuilder and(String expression, List<Object> parameters)
    {
        return condition(expression, parameters);
    }

    public ConditionBuilder and(Consumer<ConditionBuilder> consumer)
    {
        return condition(consumer);
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return or(fieldGetter, null, operator, value);
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }
        
        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return or(value, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder or(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(value, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<T, R> rightFieldGetter)
    {
        return or(leftFieldGetter, 
                null, 
                operator, 
                rightFieldGetter, 
                null);
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<T, R> rightFieldGetter,
            String rightAlias)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(leftFieldGetter, 
                leftAlias, 
                operator, 
                rightFieldGetter, 
                rightAlias, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(leftBuilder, 
                operator, 
                rightBuilder, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(builder, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }

        queryBuilder.appendCondition(value, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return or(builder, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder or(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(builder, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return or(fieldGetter, null, operator, builder);
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(Expression expression,
            String operator, 
            Object value)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(expression,
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(String expression)
    {
        return or(expression, null);
    }

    public ConditionBuilder or(String expression, List<Object> parameters)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(expression, 
                parameters, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(Consumer<ConditionBuilder> consumer)
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }

        queryBuilder.appendCondition(consumer, 
                condition, 
                getParameters());

        return this;
    }

    public String build()
    {
        return condition.toString();
    }
}
