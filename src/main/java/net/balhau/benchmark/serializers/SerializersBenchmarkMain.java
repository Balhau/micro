package net.balhau.benchmark.serializers;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmark runner for serializers
 */
public class SerializersBenchmarkMain {
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(".*" + SerializersBenchmark.class.getSimpleName() + ".*")
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }
}
