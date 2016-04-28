package bloomd;

import java.util.concurrent.CompletableFuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class BloomdClientPool {

    private final FixedChannelPool channelPool;
    private final ClientInitializer initializer;

    public BloomdClientPool(String host, int port, int maxConnections) {
        EventLoopGroup group = new NioEventLoopGroup();
        initializer = new ClientInitializer();

        Bootstrap cb = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port);

        ChannelPoolHandler poolHandler = new ChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                initializer.initChannel(ch);
            }

            @Override
            public void channelAcquired(Channel ch) throws Exception {
                initializer.unlockClient(ch);
            }

            @Override
            public void channelReleased(Channel ch) throws Exception {
                initializer.lockClient(ch);
            }
        };

        channelPool = new FixedChannelPool(cb, poolHandler, maxConnections);
    }

    public CompletableFuture<BloomdClient> acquire() {
        Future<Channel> acquire = channelPool.acquire();
        CompletableFuture<BloomdClient> client = new CompletableFuture<>();

        acquire.addListener(new GenericFutureListener<Future<Channel>>() {
            @Override
            public void operationComplete(Future<Channel> future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = future.get();
                    client.complete(initializer.getClient(channel));
                } else {
                    client.completeExceptionally(future.cause());
                }
            }
        });

        return client;
    }

    public CompletableFuture<Void> release(BloomdClient client) {
        if (client instanceof BloomdClientImpl) {
            BloomdClientImpl bloomdClient = (BloomdClientImpl) client;
            Channel ch = bloomdClient.getChannel();

            CompletableFuture<Void> releaseFuture = new CompletableFuture<>();

            channelPool.release(ch).addListener(future -> {
                if (future.isSuccess()) {
                    releaseFuture.complete(null);
                } else {
                    releaseFuture.completeExceptionally(future.cause());
                }
            });
            return releaseFuture;
        } else {
            throw new IllegalArgumentException("Unrecognized client instance: " + client);
        }
    }
}
