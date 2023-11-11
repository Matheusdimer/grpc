package com.unesc.leilao;

import com.unesc.leilao.proto.EmptyRequest;
import com.unesc.leilao.proto.LeilaoGrpc;
import com.unesc.leilao.proto.Produto;
import io.grpc.*;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeilaoClient {
    private static final Logger logger = Logger.getLogger(LeilaoClient.class.getName());

    private final LeilaoGrpc.LeilaoBlockingStub blockingStub;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public LeilaoClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = LeilaoGrpc.newBlockingStub(channel);
    }

    /** Say hello to server. */
    public void get() {
        logger.info("Will try to get products...");
        EmptyRequest request = EmptyRequest.newBuilder().build();
        try {
            Iterator<Produto> produtos = blockingStub.getProdutos(request);
            while (produtos.hasNext()) {
                Produto response = produtos.next();
                logger.info("Produto: " + response.getDescricao() + " valor: " + response.getValorMinimo());
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:9050";

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        try {
            LeilaoClient client = new LeilaoClient(channel);
            client.get();
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
