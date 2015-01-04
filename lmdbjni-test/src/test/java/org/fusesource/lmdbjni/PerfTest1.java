package org.fusesource.lmdbjni;

import org.junit.Test;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;

public class PerfTest1 {
    @Test
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerfTest1.class.getSimpleName() + ".*")
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .jvmArgs("-server")
                .jvmClasspath(Maven.classPath)
                .outputFormat(OutputFormatType.TextReport)
                .build();
        new Runner(options).run();
    }

    public static int rc;

    @GenerateMicroBenchmark
    public void mdb_cursor_get() throws IOException {
        if (rc == JNI.MDB_NOTFOUND) {
            rc = JNI.mdb_cursor_get(Setup.cursor.pointer(), Setup.keyVal, Setup.valueVal, JNI.MDB_FIRST);
        } else {
            Util.checkErrorCode(rc);
            rc = JNI.mdb_cursor_get(Setup.cursor.pointer(), Setup.keyVal, Setup.valueVal, JNI.MDB_NEXT);
        }
    }
}
