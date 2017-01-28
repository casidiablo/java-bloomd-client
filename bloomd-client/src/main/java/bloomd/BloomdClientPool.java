package bloomd;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class BloomdClientPool {

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final FixedChannelPool channelPool;
    private final ClientInitializer initializer;
    private final EventLoopGroup group;

    public BloomdClientPool(String host, int port, int maxConnections, int connectTimeoutMillis, int acquireTimeoutMillis) {
        group = new NioEventLoopGroup();
        initializer = new ClientInitializer();

        Bootstrap cb = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
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

        channelPool = new FixedChannelPool(
                cb, poolHandler, ChannelHealthChecker.ACTIVE,
                FixedChannelPool.AcquireTimeoutAction.FAIL, acquireTimeoutMillis,
                maxConnections, Integer.MAX_VALUE, true);
    }

    /**
     * Acquire a {@link BloomdClient} from this {@link BloomdClientPool}. The returned {@link Future} is notified once
     * the acquire is successful and failed otherwise.
     */
    public CompletableFuture<BloomdClient> acquire() {
        if (closed.get()) {
            throw new IllegalStateException("Pool was already closed. It can no longer be used.");
        }

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

    /**
     * Release a {@link BloomdClient} back to this {@link BloomdClientPool}. The returned {@link Future} is notified once
     * the release is successful and failed otherwise. When failed the {@link BloomdClient} connection will be automatically closed.
     */
    public CompletableFuture<Void> release(BloomdClient client) {
        if (closed.get()) {
            throw new IllegalStateException("Pool was already closed. It can no longer be used.");
        }

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

    /**
     * Closes the connections for all clients in the pool
     */
    public Future<?> closeConnections() {
        closed.set(true);
        channelPool.close();
        return group.shutdownGracefully();
    }
}
