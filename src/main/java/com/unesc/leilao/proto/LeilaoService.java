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
        if (controller.getUsuario(request.getUsername()) != null) {
            responseObserver.onNext(Response.notOk("Usuário já conectado"));
            responseObserver.onCompleted();
            return;
        }

        controller.adicionarUsuario(request.getUsername());
        String message = "Usuário " + request.getUsername() + " conectado";
        responseObserver.onNext(Response.ok(message));
        responseObserver.onCompleted();
        logger.info(message);
    }

    @Override
    public void logout(Usuario request, StreamObserver<APIResponse> responseObserver) {
        controller.removerUsuario(request.getUsername());
        responseObserver.onNext(Response.ok("Usuário desconectado"));
        responseObserver.onCompleted();
    }

    @Override
    public void getProdutos(Usuario request, StreamObserver<Produto> responseObserver) {
        controller.getProdutos(request, responseObserver);
    }

    @Override
    public void fazerLance(Lance request, StreamObserver<APIResponse> responseObserver) {
        if (request.getValor() < request.getProduto().getValorMinimo()) {
            responseObserver.onNext(Response.notOk("Lance não atingiu o valor mínimo"));
            responseObserver.onCompleted();
            return;
        }

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
