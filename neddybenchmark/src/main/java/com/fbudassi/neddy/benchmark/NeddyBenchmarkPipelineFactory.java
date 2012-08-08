package com.fbudassi.neddy.benchmark;

import com.fbudassi.neddy.benchmark.handler.HttpResponseHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import static org.jboss.netty.channel.Channels.pipeline;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;

public class NeddyBenchmarkPipelineFactory implements ChannelPipelineFactory {

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // Add necessary handlers to the pipeline.
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        pipeline.addLast("httpResponseHandler", new HttpResponseHandler());
        return pipeline;
    }
}
