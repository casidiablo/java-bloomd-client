package bloomd;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import bloomd.decoders.BloomdCommandCodec;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

public class BloomdHandler extends MessageToMessageCodec<String, Object> {

    private final Queue<BloomdCommandCodec<Object, Object>> encoders;
    private final Queue<BloomdCommandCodec<Object, Object>> decoders;

    public BloomdHandler() {
        encoders = new ConcurrentLinkedQueue<>();
        decoders = new ArrayDeque<>();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        BloomdCommandCodec<Object, Object> codec = encoders.poll();

        // send command
        String command = codec.buildCommand(msg);
        ctx.writeAndFlush(command + "\r\n");

        decoders.add(codec);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        BloomdCommandCodec<Object, Object> currentCodec = decoders.peek();
        Optional<Object> result = currentCodec.decode(msg);
        if (result.isPresent()) {
            out.add(result.get());
            decoders.poll();
        }
    }

    public <I, O> void queueCodec(BloomdCommandCodec<I, O> codec) {
        encoders.add((BloomdCommandCodec<Object, Object>) codec);
    }
}
