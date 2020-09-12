/*
 * Copyright(C) 2019, 2020. See LICENSE for more.
 */
package org.sdf.etoile;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

/**
 * Ordered union of two {@link Transformation}s.
 * Left side goes first.
 * @param <Y> Underlying data type.
 * @since 0.3.1
 */
final class OrderedUnion<Y> extends TransformationEnvelope<Row> {

    /**
     * Ctor.
     * @param left Left side.
     * @param right Right side.
     */
    OrderedUnion(final Transformation<Y> left, final Transformation<Y> right) {
        this(left.get(), right.get(), "__order");
    }

    /**
     * Ctor.
     * @param left Left side.
     * @param right Right side.
     * @param pseudo Column name for order clause.
     */
    private OrderedUnion(
        final Dataset<Y> left,
        final Dataset<Y> right,
        final String pseudo
    ) {
        super(
            new NumberedPartitions<>(
                new WithoutColumns<>(
                    new Sorted<>(
                        new Union<>(
                            left.withColumn(
                                pseudo,
                                functions.lit(0)
                            ),
                            right.withColumn(
                                pseudo,
                                functions.monotonically_increasing_id()
                            )
                        ),
                        pseudo
                    ),
                    pseudo
                ),
                1
            )
        );
    }
}
