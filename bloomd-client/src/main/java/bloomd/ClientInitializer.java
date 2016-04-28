package bloomd;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class ClientInitializer {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private final Map<Channel, BloomdClient> registry = new HashMap<>();

    public void initChannel(Channel ch) {
        // associate this new channel with a new BloomdClient implementation
        BloomdClientImpl bloomdClient = new BloomdClientImpl(ch);
        bloomdClient.setBlocked(false);
        registry.put(ch, bloomdClient);

        // reconfigure the pipeline of this channel
        ChannelPipeline pipeline = ch.pipeline();

        // Add the text line codec combination first
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(DECODER);
        pipeline.addLast(ENCODER);

        // and then business logic
        pipeline.addLast(bloomdClient.getBloomdHandler());
    }

    public BloomdClient getClient(Channel channel) {
        BloomdClient clientImpl = registry.get(channel);

        if (clientImpl == null) {
            throw new IllegalStateException("Could not find client implementation from channel");
        }

        return clientImpl;
    }

    public void unlockClient(Channel ch) {
        setClientBlocked(ch, false);
    }

    public void lockClient(Channel ch) {
        setClientBlocked(ch, true);
    }

    private void setClientBlocked(Channel ch, boolean blocked) {
        BloomdClient client = getClient(ch);

        if (client instanceof BloomdClientImpl) {
            BloomdClientImpl bloomdClient = (BloomdClientImpl) client;
            bloomdClient.setBlocked(blocked);
        }
    }
}
