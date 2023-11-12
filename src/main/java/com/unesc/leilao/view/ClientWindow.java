package com.unesc.leilao.view;

import com.unesc.leilao.proto.*;
import com.unesc.leilao.util.TableUtils;
import com.unesc.leilao.view.components.MessageConsole;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static com.unesc.leilao.util.TableUtils.*;

public class ClientWindow extends JFrame{
    private JPanel panel;
    private JTextField usernameTextField;
    private JButton entrarButton ;
    private JTable table1;
    private JComboBox<Produto> produtosCombo;
    private JButton lanceButton;
    private JTextArea logArea;
    private JFormattedTextField valorTextField;


    private final DefaultComboBoxModel<Produto> comboBoxModel = new DefaultComboBoxModel<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(PRODUTO_COLUMNS, 0);

    private final LeilaoGrpc.LeilaoBlockingStub blockingStub;

    private Usuario usuario;

    private Lance lance;
    public ClientWindow(LeilaoGrpc.LeilaoBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
        setContentPane(panel);
        setSize(800, 600);
        setTitle("Leilão");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        MessageConsole messageConsole = new MessageConsole(logArea);

        messageConsole.redirectOut();
        messageConsole.redirectErr();
        messageConsole.setMessageLines(5);
        table1.setModel(tableModel);
        produtosCombo.setModel(comboBoxModel);
        produtosCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value != null) {
                    Produto produto = (Produto) value;
                    setText(produto.getId() + " - " + produto.getDescricao());
                }
                return this;
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                APIResponse logout = blockingStub.logout(usuario);
                if (!logout.getOk()){
                    JOptionPane.showMessageDialog(ClientWindow.this, "Erro ao desconectar usuário. Por favor tente novamente!");
                }
                dispose();
            }
        });

        entrarButton.addActionListener(e -> {
            String username = getUsername();

            usuario = Usuario.newBuilder()
                    .setUsername(username)
                    .build();

            CompletableFuture.runAsync(() -> {
                fazerLogin(usuario);
            });
        });

        lanceButton.addActionListener(e -> {
            Produto selectedItem = (Produto) comboBoxModel.getSelectedItem();
            String valorLance = valorTextField.getText().replaceAll("\\.", "");

                if (valorLance.isEmpty()) {
                    JOptionPane.showMessageDialog(ClientWindow.this, "Por favor, preencha um valor para o seu lance.");
                    return;
                }

                lance = Lance.newBuilder()
                        .setProduto(selectedItem)
                        .setValor(Integer.parseInt(valorLance))
                        .setUsuario(usuario.getUsername())
                        .setDatetime(LocalDateTime.now().toString())
                        .build();

                CompletableFuture.runAsync(() -> {
                    APIResponse lanceResponse = blockingStub.fazerLance(lance);

                    if (!lanceResponse.getOk()) {
                        JOptionPane.showMessageDialog(ClientWindow.this, lanceResponse.getMessage());
                        return;
                    }
                    JOptionPane.showMessageDialog(ClientWindow.this, "Lance realizado com sucesso!");

                });
        });


    }
    public String getUsername() {
        return usernameTextField.getText();
    }

    private void fazerLogin(Usuario usuario) {
        APIResponse login = blockingStub.login(usuario);
        if (!login.getOk()){
            JOptionPane.showMessageDialog(this, login.getMessage());
            return;
        }
        entrarButton.setEnabled(false);
        usernameTextField.setEditable(false);
        carregarProdutos(usuario);
        carregarNotificacoes(usuario);
        JOptionPane.showMessageDialog(this, "Login realizado com sucesso para o usuário: " + usuario.getUsername());
    }
    private void carregarProdutos(Usuario usuario) {
        Iterator<Produto> produtos = blockingStub.getProdutos(usuario);

        new Thread(() -> {
            while (produtos.hasNext()){
                Produto produto = produtos.next();
                tableModel.addRow(TableUtils.getProdutoColumns(produto));
                comboBoxModel.addElement(produto);

                Produto selectedItem = (Produto) comboBoxModel.getSelectedItem();

            }
        }).start();
    }

    private void carregarNotificacoes(Usuario usuario) {
        new Thread(() -> {
            Iterator<Lance> lancesNotify = blockingStub.listenLances(usuario);
            while (lancesNotify.hasNext()){
                Lance lance = lancesNotify.next();

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int idAtual = (int) tableModel.getValueAt(i, 0);
                    int id = lance.getProduto().getId();

                    if (id == idAtual) {
                        tableModel.setValueAt(lance.getValor(), i, 4);
                        tableModel.setValueAt(lance.getUsuario(), i, 5);
                        LocalDateTime dateTime = LocalDateTime.parse(lance.getDatetime());
                        tableModel.setValueAt(dateTimeFormatter.format(dateTime), i, 6);
                        tableModel.setValueAt("Não", i, 7);
                        System.out.printf("O usuário %s realizou um lance no produto %s - valor: %s%n",lance.getUsuario(),lance.getProduto().getDescricao(),currency.format(lance.getValor()));
                        break;
                    }
                }
            }
        }).start();

        new Thread(() -> {
            Iterator<NotificacaoProdutoVendido> produtosNotify = blockingStub.listenProdutosVendidos(usuario);
            while (produtosNotify.hasNext()) {
                NotificacaoProdutoVendido notify = produtosNotify.next();

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int idAtual = (int) tableModel.getValueAt(i, 0);
                    int id = lance.getProduto().getId();
                    if (id == idAtual) {
                        tableModel.setValueAt("Sim", i, 7);
                        System.out.printf("O produto %s foi vendido para o usuário %s pela bagatela de %s%n", notify.getProduto().getDescricao(),notify.getUsuario(),currency.format(notify.getValor()));
                        break;
                    }
                }
            }
        }).start();
    }

    private void createUIComponents() {
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

        valorTextField = new JFormattedTextField(formatter);
    }
}
