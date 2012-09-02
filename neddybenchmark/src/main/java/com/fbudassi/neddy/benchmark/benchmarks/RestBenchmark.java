package com.fbudassi.neddy.benchmark.benchmarks;

import com.fbudassi.neddy.benchmark.pipeline.StaticContentPipelineFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fbudassi
 */
public class RestBenchmark implements Benchmark {

    private static final Logger logger = LoggerFactory.getLogger(RestBenchmark.class);

    /**
     * Rest benchmark.
     */
    @Override
    public void execute() {
    }

    /**
     * Returns the REST benchmark pipeline. For now, it's the same as the Static
     * content benchmark.
     *
     * @return
     */
    @Override
    public ChannelPipelineFactory getPipeline() {
        return new StaticContentPipelineFactory();
    }
}
