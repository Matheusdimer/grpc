package com.unesc.leilao;

import com.unesc.leilao.proto.*;
import com.unesc.leilao.view.ClientWindow;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import javax.swing.*;
import java.util.Iterator;
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

        String target = JOptionPane.showInputDialog("Informe o endereço do servidor:");
        LeilaoGrpc.LeilaoBlockingStub blockingStub = buildClient();

        ClientWindow clientWindow = new ClientWindow(blockingStub);
    }
}
