package com.unesc.leilao;

import com.unesc.leilao.controller.LeilaoController;
import com.unesc.leilao.proto.LeilaoService;
import com.unesc.leilao.proto.Produto;
import com.unesc.leilao.view.ServerWindow;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LeilaoServer {
    private static final Logger logger = Logger.getLogger(LeilaoServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 9050;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new LeilaoService())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                LeilaoServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            LeilaoController.getInstance().closeAllConnections();
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        new ServerWindow();
        final LeilaoServer server = new LeilaoServer();
        server.start();
        server.blockUntilShutdown();
    }
}
