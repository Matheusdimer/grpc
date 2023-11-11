package com.unesc.leilao;

import com.unesc.leilao.proto.LeilaoGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LeilaoClient {
    private static final Logger logger = Logger.getLogger(LeilaoClient.class.getName());

    private static final String target = "localhost:9050";

    public static LeilaoGrpc.LeilaoBlockingStub buildClient() {
        logger.info("Iniciando conexão com o servidor " + target);
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        return LeilaoGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws Exception {
        LeilaoGrpc.LeilaoBlockingStub blockingStub = buildClient();

        try {
            // TODO ponto de partida
        } finally {
            ((ManagedChannel) blockingStub.getChannel()).shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
