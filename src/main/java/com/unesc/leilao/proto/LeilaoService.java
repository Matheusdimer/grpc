package com.unesc.leilao.proto;

import com.unesc.leilao.controller.LeilaoController;
import com.unesc.leilao.util.Response;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class LeilaoService extends LeilaoGrpc.LeilaoImplBase {

    private static final Logger logger = Logger.getLogger(LeilaoService.class.getName());

    private final LeilaoController controller = LeilaoController.getInstance();

    @Override
    public void login(Usuario request, StreamObserver<APIResponse> responseObserver) {
        String message = "Usuário " + request.getUsername() + " conectado";
        controller.adicionarUsuario(request.getUsername());
        responseObserver.onNext(Response.ok(message));
        responseObserver.onCompleted();
        logger.info(message);
    }

    @Override
    public void getProdutos(Usuario request, StreamObserver<Produto> responseObserver) {
        controller.getProdutos(request, responseObserver);
    }

    @Override
    public void fazerLance(Lance request, StreamObserver<APIResponse> responseObserver) {
        controller.fazerLance(request);
        responseObserver.onNext(Response.ok("Lance registrado com sucesso."));
        responseObserver.onCompleted();
    }

    @Override
    public void listenLances(Usuario request, StreamObserver<Lance> responseObserver) {
        controller.registrarListenerLances(request, responseObserver);
    }

    @Override
    public void listenProdutosVendidos(Usuario request, StreamObserver<NotificacaoProdutoVendido> responseObserver) {
        controller.registrarListenerProdutosVendidos(request, responseObserver);
    }
}
