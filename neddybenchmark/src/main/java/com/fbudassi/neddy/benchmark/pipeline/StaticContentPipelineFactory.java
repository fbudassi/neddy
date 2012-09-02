package com.fbudassi.neddy.benchmark.pipeline;

import com.fbudassi.neddy.benchmark.handler.StaticContentBenchmarkHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import static org.jboss.netty.channel.Channels.pipeline;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

/**
 *
 * @author fbudassi
 */
public class StaticContentPipelineFactory implements ChannelPipelineFactory {

    /**
     * Gets the necessary Pipeline Factory for a static content benchmark.
     *
     * @return
     * @throws Exception
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // Add necessary handlers to the pipeline.
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        pipeline.addLast("httpResponseHandler", new StaticContentBenchmarkHandler());
        return pipeline;
    }
}
