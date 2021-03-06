/*
 * Copyright(C) 2019, 2020. See LICENSE for more.
 */
package org.sdf.etoile;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sdf.etoile.expr.Expression;
import scala.collection.JavaConverters;

/**
 * Repartitioned.
 * @param <X> Underlying data type.
 * @since 0.6.0
 */
public final class Repartitioned<X> extends TransformationEnvelope<X> {
    /**
     * Ctor.
     * @param original Original transfrormation.
     * @param partitions Partition expressions.
     */
    public Repartitioned(final Transformation<X> original,
        final Expression... partitions) {
        super(
            () -> original.get().repartition(
                JavaConverters.asScalaBuffer(
                    Stream.of(partitions)
                        .map(Expression::get)
                        .collect(Collectors.toList())
                )
            )
        );
    }
}
