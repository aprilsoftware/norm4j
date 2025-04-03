package org.norm4j;

import java.util.List;

public interface Expression
{
    public String build(TableManager tableManager, List<Object> parameters);
}
