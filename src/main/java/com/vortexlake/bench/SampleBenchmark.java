package com.vortexlake.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class SampleBenchmark {

    @Param({"10", "100", "1000"})
    private int size;

    private int[] data;

    @Setup
    public void setup() {
        data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i;
        }
    }

    @Benchmark
    public void baselineSum(Blackhole bh) {
        int sum = 0;
        for (int d : data) {
            sum += d;
        }
        bh.consume(sum);
    }
}
