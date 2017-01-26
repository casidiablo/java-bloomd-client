package bloomd;

import bloomd.decoders.BloomdCommandCodec;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BloomdHandler extends MessageToMessageCodec<String, Object> {

    private final Queue<BloomdCommandCodec<Object, Object>> encoders;
    private final Queue<BloomdCommandCodec<Object, Object>> decoders;
    private final OnReplyReceivedListener onReplyReceivedListener;

    public BloomdHandler(OnReplyReceivedListener onReplyReceivedListener) {
        this.onReplyReceivedListener = onReplyReceivedListener;
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

        Object result = null;
        Exception failure;
        try {
            result = currentCodec.decode(msg);
            failure = null;
        } catch (Exception e) {
            failure = e;
        }

        if (failure != null) {
            onReplyReceivedListener.onError(failure);
            decoders.poll();
        } else if (result != null) {
            onReplyReceivedListener.onReplyReceived(result);
            decoders.poll();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        onReplyReceivedListener.onDisconnect();
    }

    public <I, O> void queueCodec(BloomdCommandCodec<I, O> codec) {
        encoders.add((BloomdCommandCodec<Object, Object>) codec);
    }

    public interface OnReplyReceivedListener {
        void onReplyReceived(Object reply);

        void onDisconnect();

        void onError(Exception e);
    }
}
