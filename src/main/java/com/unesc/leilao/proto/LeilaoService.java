package com.unesc.leilao.proto;

import io.grpc.stub.StreamObserver;

public class LeilaoService extends LeilaoGrpc.LeilaoImplBase {
//    @Override
//    public void getProdutos(Empty request, StreamObserver<Produto> responseObserver) {
//        Produto produto = Produto.newBuilder()
//                .setDescricao("Teste")
//                .setValorMinimo(50)
//                .build();
//        responseObserver.onNext(produto);
//        responseObserver.onCompleted();
//    }


    @Override
    public void getProdutos(EmptyRequest request, StreamObserver<Produto> responseObserver) {
        int i = 1;

        while (true) {
            Produto produto = Produto.newBuilder()
                .setDescricao("Produto " + i)
                .setValorMinimo(50 + i)
                .build();
            responseObserver.onNext(produto);
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                responseObserver.onCompleted();
                throw new RuntimeException(e);
            }
        }
    }
}
