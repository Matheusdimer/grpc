package com.unesc.leilao.controller;

import com.unesc.leilao.proto.Lance;
import com.unesc.leilao.proto.NotificacaoProdutoVendido;
import com.unesc.leilao.proto.Produto;
import io.grpc.stub.StreamObserver;

public class UsuarioConectado {

    private final String username;

    private StreamObserver<Produto> produtosStream;

    private StreamObserver<Lance> notificacaoLanceStream;

    private StreamObserver<NotificacaoProdutoVendido> notificacaoProdutoVendidoStream;

    public UsuarioConectado(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public StreamObserver<Produto> getProdutosStream() {
        return produtosStream;
    }

    public void setProdutosStream(StreamObserver<Produto> produtosStream) {
        this.produtosStream = produtosStream;
    }

    public StreamObserver<Lance> getNotificacaoLanceStream() {
        return notificacaoLanceStream;
    }

    public void setNotificacaoLanceStream(StreamObserver<Lance> notificacaoLanceStream) {
        this.notificacaoLanceStream = notificacaoLanceStream;
    }

    public StreamObserver<NotificacaoProdutoVendido> getNotificacaoProdutoVendidoStream() {
        return notificacaoProdutoVendidoStream;
    }

    public void setNotificacaoProdutoVendidoStream(StreamObserver<NotificacaoProdutoVendido> notificacaoProdutoVendidoStream) {
        this.notificacaoProdutoVendidoStream = notificacaoProdutoVendidoStream;
    }
}
