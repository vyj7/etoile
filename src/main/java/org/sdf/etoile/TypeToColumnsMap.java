/*
 * Copyright(C) 2019
 */
package org.sdf.etoile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cactoos.collection.Joined;
import org.cactoos.map.MapEnvelope;

/**
 * Map of DataTypes to columns.
 *
 * @since 0.3.0
 */
final class TypeToColumnsMap extends MapEnvelope<DataType, List<String>> {

    /**
     * Ctor.
     * @param transformation Tranformation from which to extract mapping.
     */
    TypeToColumnsMap(final Transformation<Row> transformation) {
        this(transformation.get().schema());
    }

    /**
     * Ctor.
     * @param schema Schema.
     */
    private TypeToColumnsMap(final StructType schema) {
        super(
            () -> {
                final Function<StructField, DataType> type = StructField::dataType;
                final Function<StructField, String> name = StructField::name;
                return Stream.of(schema.fields())
                    .collect(
                        Collectors.toMap(
                            type,
                            name.andThen(Collections::singletonList),
                            (a, b) -> new ArrayList<>(new Joined<>(a, b))
                        )
                    );
            }
        );
    }

}
