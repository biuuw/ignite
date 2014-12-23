/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.query.h2.sql;

import org.h2.command.*;
import org.jetbrains.annotations.*;

/**
 * Alias for column or table.
 */
public class GridAlias extends GridSqlElement {

    /** */
    private final String alias;

    /** */
    private final boolean useAs;

    /**
     * @param alias Alias.
     * @param expr Expr.
     */
    public GridAlias(@NotNull String alias, @NotNull GridSqlElement expr) {
        this(alias, expr, false);
    }

    /**
     * @param alias Alias.
     * @param expr Expr.
     * @param useAs Use 'AS' keyword.
     */
    public GridAlias(@NotNull String alias, @NotNull GridSqlElement expr, boolean useAs) {
        addChild(expr);

        this.useAs = useAs;
        this.alias = alias;
    }

    /** {@inheritDoc} */
    @Override public String getSQL() {
        return child().getSQL() + (useAs ? " AS " : " ") + Parser.quoteIdentifier(alias);
    }

    /**
     * @return Alias.
     */
    @NotNull
    public String alias() {
        return alias;
    }

    /** {@inheritDoc} */
    public GridSqlElement getNonAliasExpression() {
        return child();
    }
}
