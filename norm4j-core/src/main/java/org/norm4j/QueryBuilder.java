package org.norm4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public abstract class QueryBuilder
{
    private final TableManager tableManager;
    private final List<Object> parameters;

    public QueryBuilder(TableManager tableManager)
    {
        this.tableManager = tableManager;

        parameters = new ArrayList<>();
    }

    protected TableManager getTableManager()
    {
        return tableManager;
    }

    protected List<Object> getParameters()
    {
        return parameters;
    }

    protected <T, R> void appendCondition(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value,
            StringBuilder condition, 
            List<Object> parameters)
    {
        append(fieldGetter, alias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        if (value == null)
        {
            condition.append("NULL");
        }
        else
        {
            condition.append("?");

            getParameters().add(value);
        }
    }

    protected <T, R> void appendCondition(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias,
            StringBuilder condition, 
            List<Object> parameters)
    {
        if (value == null)
        {
            condition.append("NULL");
        }
        else
        {
            condition.append("?");

            getParameters().add(value);
        }

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        append(fieldGetter, alias, condition);
    }

    protected <T, R> void appendCondition(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<T, R> rightFieldGetter,
            String rightAlias,
            StringBuilder condition, 
            List<Object> parameters)
    {
        append(leftFieldGetter, leftAlias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        append(rightFieldGetter, rightAlias, condition);
    }

    protected void appendCondition(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder,
            StringBuilder condition, 
            List<Object> parameters)
    {
        condition.append("(");
        condition.append(leftBuilder.build());
        condition.append(")");

        getParameters().addAll(leftBuilder.getParameters());

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");
        condition.append(rightBuilder.build());
        condition.append(")");

        getParameters().addAll(rightBuilder.getParameters());
    }

    protected void appendCondition(SelectQueryBuilder builder,
            String operator, 
            Object value,
            StringBuilder condition, 
            List<Object> parameters)
    {
        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        if (value == null)
        {
            condition.append("NULL");
        }
        else
        {
            condition.append("?");

            getParameters().add(value);
        }
    }

    protected void appendCondition(Object value,
            String operator, 
            SelectQueryBuilder builder,
            StringBuilder condition, 
            List<Object> parameters)
    {
        if (value == null)
        {
            condition.append("NULL");
        }
        else
        {
            condition.append("?");

            getParameters().add(value);
        }

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());
    }

    protected <T, R> void appendCondition(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias,
            StringBuilder condition, 
            List<Object> parameters)
    {
        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        append(fieldGetter, alias, condition);
    }

    protected <T, R> void appendCondition(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder,
            StringBuilder condition, 
            List<Object> parameters)
    {
        append(fieldGetter, alias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());
    }

    protected <T, R> void appendCondition(Expression expression,
            String operator, 
            Object value,
            StringBuilder condition, 
            List<Object> parameters)
    {
        condition.append(expression.build(tableManager, parameters));

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        if (value == null)
        {
            condition.append("NULL");
        }
        else
        {
            condition.append("?");

            getParameters().add(value);
        }
    }

    protected void appendCondition(String expression, 
            List<Object> expressionParameters, 
            StringBuilder condition, 
            List<Object> parameters)
    {
        condition.append(expression);

        if (expressionParameters != null)
        {
            parameters.addAll(expressionParameters);
        }
    }

    protected void appendCondition(Consumer<ConditionBuilder> consumer, 
            StringBuilder condition, 
            List<Object> parameters)
    {
        ConditionBuilder builder;

        builder = new ConditionBuilder(this);

        consumer.accept(builder);

        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        parameters.addAll(builder.getParameters());
    }

    protected <T, R> void append(FieldGetter<T, R> fieldGetter, 
            String alias, 
            StringBuilder sb)
    {
        append(tableManager.getMetadataManager().getMetadata(fieldGetter), 
                alias, 
                sb);
    }

    protected <T, R> void append(ColumnMetadata column, 
            String alias, 
            StringBuilder sb)
    {
        if (alias == null)
        {
            sb.append(tableManager.getDialect()
                    .getTableName(column.getTable()));
            sb.append(".");
            sb.append(column.getColumnName());
        }
        else
        {
            sb.append(alias);
            sb.append(".");
            sb.append(column.getColumnName());
        }
    }

    protected TableMetadata getTable(Class<?> tableClass)
    {
        TableMetadata table;

        table = tableManager.getMetadataManager().getMetadata(tableClass);

        if (table == null)
        {
            throw new IllegalArgumentException("No metadata found for class " 
                    + tableClass.getName());
        }
        else
        {
            return table;
        }
    }
}
