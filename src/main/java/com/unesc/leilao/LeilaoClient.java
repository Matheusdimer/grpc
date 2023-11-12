package com.unesc.leilao;

import com.unesc.leilao.proto.LeilaoGrpc;
import com.unesc.leilao.view.ClientWindow;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.logging.Logger;

public class LeilaoClient {
    private static final Logger logger = Logger.getLogger(LeilaoClient.class.getName());

    private static String target = "localhost:9050";

    public static LeilaoGrpc.LeilaoBlockingStub buildClient() {
        logger.info("Iniciando conexão com o servidor " + target);
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        return LeilaoGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        String text = JOptionPane.showInputDialog("Informe o endereço do servidor:");

        if (StringUtils.isNotBlank(text)) {
            target = text;
        }

        LeilaoGrpc.LeilaoBlockingStub blockingStub = buildClient();
        new ClientWindow(blockingStub);
    }
}
