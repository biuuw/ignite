/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.query.h2.sql;

import org.h2.util.*;

/**
 *
 */
public enum GridOperationType {
    // from org.h2.expression.Operation
    CONCAT(2, new BiExpressionSqlGenerator("||")),
    PLUS(2, new BiExpressionSqlGenerator("+")),
    MINUS(2, new BiExpressionSqlGenerator("-")),
    MULTIPLY(2, new BiExpressionSqlGenerator("*")),
    DIVIDE(2, new BiExpressionSqlGenerator("/")),
    MODULUS(2, new BiExpressionSqlGenerator("%")),
    NEGATE(1, new PrefixSqlGenerator("-")),

    // from org.h2.expression.Comparison
    EQUAL(2, new BiExpressionSqlGenerator("=")),
    EQUAL_NULL_SAFE(2, new BiExpressionSqlGenerator("IS")),
    BIGGER_EQUAL(2, new BiExpressionSqlGenerator(">=")),
    BIGGER(2, new BiExpressionSqlGenerator(">")),
    SMALLER_EQUAL(2, new BiExpressionSqlGenerator("<=")),
    SMALLER(2, new BiExpressionSqlGenerator("<")),
    NOT_EQUAL(2, new BiExpressionSqlGenerator("<>")),
    NOT_EQUAL_NULL_SAFE(2, new BiExpressionSqlGenerator("IS NOT")),

    SPATIAL_INTERSECTS(2, new IntersectsSqlGenerator()),
    IS_NULL(1, new SuffixSqlGenerator("IS NULL")),
    IS_NOT_NULL(1, new SuffixSqlGenerator("IS NOT NULL")),

    NOT(1, new PrefixSqlGenerator("NOT")),

    // from org.h2.expression.ConditionAndOr
    AND(2, new BiExpressionSqlGenerator("AND")),
    OR(2, new BiExpressionSqlGenerator("OR")),

    // from
    REGEXP(2, new BiExpressionSqlGenerator("REGEXP")),
    LIKE(2, new BiExpressionSqlGenerator("LIKE")),

    IN(-1, new ConditionInSqlGenerator()),

    ;
    /** */
    private final SqlGenerator sqlGenerator;

    /** */
    private final int childrenCnt;

    /**
     * @param childrenCnt Children count.
     * @param sqlGenerator sqlGenerator.
     */
    GridOperationType(int childrenCnt, SqlGenerator sqlGenerator) {
        this.childrenCnt = childrenCnt;
        this.sqlGenerator = sqlGenerator;
    }

    /**
     * @param operation Operation.
     */
    public String toSql(GridOperation operation) {
        return sqlGenerator.getSql(operation);
    }

    /**
     * @return Children count.
     */
    public int childrenCount() {
        return childrenCnt;
    }

    /**
     *
     */
    private static interface SqlGenerator {

        /**
         * @param operation Operation expression.
         */
        public String getSql(GridOperation operation);
    }

    /**
     *
     */
    private static class BiExpressionSqlGenerator implements SqlGenerator {

        /** */
        private final String delim;

        /**
         * @param delim Delimiter.
         */
        private BiExpressionSqlGenerator(String delim) {
            this.delim = delim;
        }

        /** {@inheritDoc} */
        @Override public String getSql(GridOperation operation) {
            assert operation.opType().childrenCnt == 2;

            return '(' + operation.child(0).getSQL() + " " + delim + " " + operation.child(1).getSQL() + ')';
        }
    }

    /**
     *
     */
    private static class IntersectsSqlGenerator implements SqlGenerator {

        /** {@inheritDoc} */
        @Override public String getSql(GridOperation operation) {
            assert operation.opType().childrenCnt == 2;

            return "(INTERSECTS(" + operation.child(0) + ", " + operation.child(1) + "))";
        }
    }

    /**
     *
     */
    private static class PrefixSqlGenerator implements SqlGenerator {
        /** */
        private final String text;

        /**
         * @param text Text.
         */
        private PrefixSqlGenerator(String text) {
            this.text = text;
        }

        /** {@inheritDoc} */
        @Override public String getSql(GridOperation operation) {
            assert operation.opType().childrenCnt == 1;

            return '(' + text + ' ' + operation.child().getSQL() + ')';
        }
    }

    /**
     *
     */
    private static class SuffixSqlGenerator implements SqlGenerator {
        /** */
        private final String text;

        /**
         * @param text Text.
         */
        private SuffixSqlGenerator(String text) {
            this.text = text;
        }

        /** {@inheritDoc} */
        @Override public String getSql(GridOperation operation) {
            assert operation.opType().childrenCnt == 1;

            return '(' + operation.child().getSQL() + ' ' + text + ')';
        }
    }

    /**
     *
     */
    private static class ConditionInSqlGenerator implements SqlGenerator {

        /** {@inheritDoc} */
        @Override public String getSql(GridOperation operation) {
            StatementBuilder buff = new StatementBuilder("(");

            buff.append(operation.child(0).getSQL()).append(" IN(");

            assert operation.children().size() > 1;

            if (operation.children().size() == 2) {
                String child = operation.child(1).getSQL();

                buff.append(' ').append(StringUtils.unEnclose(child)).append(' ');
            }
            else {
                for (int i = 1; i < operation.children().size(); i++) {
                    buff.appendExceptFirst(", ");
                    buff.append(operation.child(i).getSQL());
                }
            }

            return buff.append("))").toString();
        }
    }
}
