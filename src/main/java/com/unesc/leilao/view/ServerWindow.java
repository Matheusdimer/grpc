package com.unesc.leilao.view;

import com.unesc.leilao.view.components.MessageConsole;

import javax.swing.*;

public class ServerWindow extends JFrame {
    private JPanel panel;
    private JTable table;
    private JTextField descricaoField;
    private JTextField valorField;
    private JButton cadastrarProdutoButton;
    private JScrollPane logScrollPane;
    private JTextPane logPane;

    public ServerWindow() {
        super("Painel de controle do leilão");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        MessageConsole messageConsole = new MessageConsole(logPane);
        messageConsole.redirectOut();
        messageConsole.redirectErr();
        messageConsole.setMessageLines(20);
    }
}
