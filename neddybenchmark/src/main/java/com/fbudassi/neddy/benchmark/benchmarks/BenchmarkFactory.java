package com.fbudassi.neddy.benchmark.benchmarks;

import com.fbudassi.neddy.benchmark.NeddyBenchmark.ParameterEnum;

/**
 * Factory class to build benchmark classes.
 *
 * @author fbudassi
 */
public class BenchmarkFactory {

    /**
     * Factory method to build benchmarks depending on the user's selection.
     *
     * @param benchmark
     * @return
     * @throws Exception
     */
    public static Benchmark getBenchmark(ParameterEnum benchmark) throws Exception {
        if (benchmark == null) {
            throw new Exception("Benchmark type not defined");
        }

        switch (benchmark) {
            case STATIC:
                return new StaticContentBenchmark();
            case WS:
                return new WebsocketBenchmark();
            case REST:
                return new RestBenchmark();
        }

        // This should never happen.
        return null;
    }
}
