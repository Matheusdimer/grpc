package com.unesc.leilao.controller;

import com.unesc.leilao.proto.Lance;
import com.unesc.leilao.proto.NotificacaoProdutoVendido;
import com.unesc.leilao.proto.Produto;
import com.unesc.leilao.proto.Usuario;
import com.unesc.leilao.util.TaskUtil;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.function.Consumers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.unesc.leilao.util.TableUtils.currency;

public class LeilaoController {

    private static final Logger logger = Logger.getLogger(LeilaoController.class.getName());

    private static final int TEMPO_LEILAO = 1;

    private static final LeilaoController instance = new LeilaoController();

    private final Map<String, UsuarioConectado> usuariosConectados = new ConcurrentHashMap<>();

    private final Map<Integer, Produto> produtos = new ConcurrentHashMap<>();

    private final Map<Integer, TaskUtil.Task> lancesTasks = new ConcurrentHashMap<>();

    private Consumer<Produto> onProdutoCadastrado = Consumers.nop();

    private Consumer<Lance> onLance = Consumers.nop();

    private Consumer<Produto> onProdutoVendido = Consumers.nop();


    public static LeilaoController getInstance() {
        return instance;
    }

    public void adicionarUsuario(String username) {
        usuariosConectados.put(username, new UsuarioConectado(username));
    }

    public void removerUsuario(String username) {
        UsuarioConectado usuarioConectado = usuariosConectados.get(username);
        if (usuarioConectado == null) {
            return;
        }

        closeConnection(usuarioConectado.getProdutosStream());
        closeConnection(usuarioConectado.getNotificacaoLanceStream());
        closeConnection(usuarioConectado.getNotificacaoProdutoVendidoStream());
        usuariosConectados.remove(username);
        logger.info("Usuário " + username + " desconectado");
    }

    public UsuarioConectado getUsuario(String username) {
        return usuariosConectados.get(username);
    }

    public Produto getProduto(int id) {
        return produtos.get(id);
    }

    public void cadastrarProduto(Produto produto) {
        int id = produtos.size() + 1;
        produto = produto.toBuilder()
                .setId(id)
                .setDatetime(LocalDateTime.now().toString())
                .build();
        produtos.put(id, produto);
        notificarProdutoCadastrado(produto);
        logger.info("Produto " + produto.getDescricao() + " cadastrado");
    }

    public void fazerLance(Lance lance) {
        int idProduto = lance.getProduto().getId();
        Produto produto = produtos.get(idProduto).toBuilder()
                .setUltimoLance(lance)
                .build();
        produtos.replace(idProduto, produto);
        registrarLance(lance);
        notificarLance(lance);
        logger.info("Lance realizado pelo usuário " + lance.getUsuario() + " para o produto " + produto.getDescricao());
    }

    public void notificarProdutoCadastrado(Produto produto) {
        usuariosConectados.forEach((username, usuarioConectado) ->
                notifyObserver(usuarioConectado, usuarioConectado.getProdutosStream(), produto));
        CompletableFuture.runAsync(() -> onProdutoCadastrado.accept(produto));
    }

    public synchronized void notificarLance(Lance lance) {
        usuariosConectados.forEach((username, usuarioConectado) ->
                notifyObserver(usuarioConectado, usuarioConectado.getNotificacaoLanceStream(), lance));
        CompletableFuture.runAsync(() -> onLance.accept(lance));
    }

    public synchronized void notificarProdutoVendido(NotificacaoProdutoVendido notificacaoProdutoVendido) {
        usuariosConectados.forEach((username, usuarioConectado) ->
                notifyObserver(usuarioConectado, usuarioConectado.getNotificacaoProdutoVendidoStream(), notificacaoProdutoVendido));
        CompletableFuture.runAsync(() -> onProdutoVendido.accept(notificacaoProdutoVendido.getProduto()));
    }

    public void getProdutos(Usuario usuario, StreamObserver<Produto> responseObserver) {
        UsuarioConectado usuarioConectado = usuariosConectados.get(usuario.getUsername());

        if (usuarioConectado == null) {
            responseObserver.onError(new RuntimeException("Usuário não conectado."));
            return;
        }

        usuarioConectado.setProdutosStream(responseObserver);
        produtos.forEach((integer, produto) -> responseObserver.onNext(produto));
    }

    public void registrarListenerLances(Usuario request, StreamObserver<Lance> responseObserver) {
        UsuarioConectado usuarioConectado = getUsuario(request.getUsername());
        usuarioConectado.setNotificacaoLanceStream(responseObserver);
    }

    public void registrarListenerProdutosVendidos(Usuario request, StreamObserver<NotificacaoProdutoVendido> responseObserver) {
        UsuarioConectado usuarioConectado = getUsuario(request.getUsername());
        usuarioConectado.setNotificacaoProdutoVendidoStream(responseObserver);
    }

    public void closeAllConnections() {
        System.err.println("Encerrando conexões de todos os usuários ativos");
        usuariosConectados.forEach((username, usuarioConectado) -> {
            closeConnection(usuarioConectado.getProdutosStream());
            closeConnection(usuarioConectado.getNotificacaoLanceStream());
            closeConnection(usuarioConectado.getNotificacaoProdutoVendidoStream());
        });
        System.err.println("Conexões encerradas");
    }

    private <T> void notifyObserver(UsuarioConectado usuario, StreamObserver<T> streamObserver, T value) {
        if (streamObserver != null) {
            try {
                streamObserver.onNext(value);
            } catch (StatusRuntimeException exception) {
                removerUsuario(usuario.getUsername());
            }
        }
    }

    private void closeConnection(StreamObserver<?> streamObserver) {
        if (streamObserver != null) {
            streamObserver.onCompleted();
        }
    }

    public void setOnProdutoCadastrado(Consumer<Produto> onProdutoCadastrado) {
        this.onProdutoCadastrado = onProdutoCadastrado;
    }

    public void setOnLance(Consumer<Lance> onLance) {
        this.onLance = onLance;
    }

    public void setOnProdutoVendido(Consumer<Produto> onProdutoVendido) {
        this.onProdutoVendido = onProdutoVendido;
    }

    /**
     * Registra uma nova task para terminar o leilão do produto em X minutos caso não haja mais nenhum lance
     */
    private void registrarLance(Lance lance) {
        int idProduto = lance.getProduto().getId();

        Optional.ofNullable(lancesTasks.get(idProduto))
                .ifPresent(TaskUtil.Task::cancel);

        lancesTasks.put(idProduto, TaskUtil.futureTask(() -> terminarLeilao(lance), TEMPO_LEILAO));
    }

    private void terminarLeilao(Lance lance) {
        int idProduto = lance.getProduto().getId();
        Produto produto = produtos.get(idProduto);

        produto = produto.toBuilder()
                .setVendido(true)
                .build();
        produtos.replace(idProduto, produto);

        NotificacaoProdutoVendido notificacaoProdutoVendido = NotificacaoProdutoVendido.newBuilder()
                .setProduto(produto)
                .setValor(lance.getValor())
                .setUsuario(lance.getUsuario())
                .build();
        notificarProdutoVendido(notificacaoProdutoVendido);
        logger.info(String.format("Produto %s vendido para %s por %s",
                produto.getDescricao(),
                lance.getUsuario(),
                currency.format(lance.getValor())
        ));
    }
}
