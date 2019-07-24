package org.sdf.etoile;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.jdbc.JdbcDialects;

import java.net.URI;
import java.util.Map;


@RequiredArgsConstructor
public final class Main implements Runnable {
    private final SparkSession spark;
    private final Map<String, String> args;

    public static void main(final String[] args) {
        JdbcDialects.registerDialect(new ExtraOracleDialect());
        new Main(SparkSession.builder()
                .getOrCreate(), new Args(args)).run();
    }

    @Override
    public void run() {
        final Map<String, String> inOpts = new PrefixArgs("input", this.args);
        final Map<String, String> outOpts = new PrefixArgs("output", this.args);
        final Transformation<Row> input = new Input(this.spark, inOpts);
        final Transformation<Row> replaced =
                new ConditionalTransformation<>(
                        () -> inOpts.containsKey("replace"),
                        new Substituted(
                                input,
                                new ReplacementMap(
                                        inOpts.get("replace")
                                )
                        ),
                        input
                );
        final Transformation<Row> casted = new FullyCastedByParameters(
                replaced,
                inOpts
        );
        final Transformation<Row> sorted = new SortedByParameter<>(
                casted, inOpts
        );
        final Transformation<Row> castedAgain = new FullyCastedByParameters(
                sorted,
                outOpts
        );
        final Transformation<Row> dropped = new ColumnsDroppedByParameter<>(
                castedAgain,
                outOpts
        );
        final Transformation<Row> repartitioned = new NumberedPartitions<>(
                dropped,
                Integer.parseUnsignedInt(
                        outOpts.getOrDefault("partitions", "1")
                )
        );
        final Transformation<Row> outReplaced =
                new ConditionalTransformation<>(
                        () -> outOpts.containsKey("replace"),
                        new Substituted(
                                input,
                                new ReplacementMap(
                                        outOpts.get("replace")
                                )
                        ),
                        input
                );
        final Output<Row> output = new FormatOutput<>(
                repartitioned,
                outOpts
        );
        final Output<Row> mode = new Mode<>(
                outOpts.getOrDefault(
                        "mode",
                        SaveMode.ErrorIfExists.name()
                ),
                output
        );
        final Terminal saved = new Saved<>(
                URI.create(outOpts.get("path")),
                mode
        );
        saved.result();
    }
}


