package com.fbudassi.neddy.benchmark.benchmarks;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * To be implemented by all the possible benchmarks that can execute
 * NeddyBenchmark.
 *
 * @author fbudassi
 */
public interface Benchmark {

    /**
     * Executes the benchmark.
     */
    public void execute() throws Exception;

    /**
     * Return the necessary pipeline for this benchmark.
     */
    public ChannelPipelineFactory getPipeline();

    /**
     * Used to configure the bootstrap's socket options.
     */
    public void configureBootstrap(ClientBootstrap bootstrap);
}
