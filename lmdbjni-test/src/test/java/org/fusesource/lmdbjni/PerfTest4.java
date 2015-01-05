package org.fusesource.lmdbjni;

import org.junit.Test;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PerfTest4 {
    static Cursor cursor;
    static Transaction tx;
    static {
        tx = Setup.env.createTransaction();
        cursor = Setup.database.openCursor(tx);
    }

    @Test
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerfTest4.class.getSimpleName() + ".*")
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .jvmArgs("-server")
                .jvmClasspath(Maven.classPath)
                .outputFormat(OutputFormatType.TextReport)
                .build();
        new Runner(options).run();
    }

    public static AtomicLong counter = new AtomicLong(0);

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void mdb_cursor_put() throws IOException {
        Setup.database.put(tx, Bytes.fromLong(counter.incrementAndGet()), Bytes.fromLong(counter.get()));
    }
}
