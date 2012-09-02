package com.fbudassi.neddy.benchmark.benchmarks;

import com.fbudassi.neddy.benchmark.config.Config;
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
    // Configuration constants.
    private static final int NUMSPEAKERS = Config.getIntValue(Config.KEY_NUMSPEAKERS);
    private static final int NUMCATEGORIES = Config.getIntValue(Config.KEY_SPEAKER_NUMCATEGORIES);
    private static final int DELAYBETWEENMESSAGES = Config.getIntValue(Config.KEY_SPEAKER_DELAYBETWEENMESSAGES);
    private static final String RESOURCE_CATEGORY = Config.getValue(Config.KEY_RESOURCE_CATEGORY);
    private static final String RESOURCE_LISTENERS = Config.getValue(Config.KEY_RESOURCE_LISTENERS);

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
