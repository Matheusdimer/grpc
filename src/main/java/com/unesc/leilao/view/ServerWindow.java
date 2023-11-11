package com.unesc.leilao.view;

import com.unesc.leilao.controller.LeilaoController;
import com.unesc.leilao.proto.Lance;
import com.unesc.leilao.proto.Produto;
import com.unesc.leilao.util.TableUtils;
import com.unesc.leilao.view.components.MessageConsole;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;

import static com.unesc.leilao.util.TableUtils.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ServerWindow extends JFrame {

    private JPanel panel;
    private JTable table;
    private JTextField descricaoField;
    private JTextField valorField;
    private JButton cadastrarProdutoButton;
    private JTextPane logPane;
    private final DefaultTableModel tableModel = new DefaultTableModel(PRODUTO_COLUMNS, 0);
    private final LeilaoController controller = LeilaoController.getInstance();

    public ServerWindow() {
        super("Painel de controle do leilão");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        MessageConsole messageConsole = new MessageConsole(logPane);
        messageConsole.redirectOut();
        messageConsole.redirectErr();
        messageConsole.setMessageLines(20);

        table.setModel(tableModel);
        cadastrarProdutoButton.addActionListener(this::cadastrarProduto);
        controller.setOnProdutoCadastrado(this::onProdutoCadastrado);
        controller.setOnLance(this::onLance);
    }

    private void cadastrarProduto(ActionEvent event) {
        String descricao = descricaoField.getText();
        String valor = valorField.getText().replaceAll("\\.", "");

        if (isBlank(descricao) || isBlank(valor)) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha os campos do produto.");
            return;
        }

        Produto produto = Produto.newBuilder()
                .setDescricao(descricao)
                .setValorMinimo(Double.parseDouble(valor))
                .setVendido(false)
                .setDatetime(LocalDateTime.now().toString())
                .build();
        controller.cadastrarProduto(produto);

        descricaoField.setText("");
        valorField.setText("");
    }

    private void createUIComponents() {
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

        valorField = new JFormattedTextField(formatter);
    }

    private void onLance(Lance lance) {
        int produtoId = lance.getProduto().getId();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int id = (int) tableModel.getValueAt(i, 0);

            if (produtoId == id) {
                LocalDateTime dateTime = LocalDateTime.parse(lance.getDatetime());
                tableModel.setValueAt(currency.format(lance.getValor()), i, 4);
                tableModel.setValueAt(lance.getUsuario(), i, 5);
                tableModel.setValueAt(dateTimeFormatter.format(dateTime), i, 6);
                break;
            }
        }
    }

    private void onProdutoCadastrado(Produto produto) {
        tableModel.addRow(TableUtils.getProdutoColumns(produto));
    }
}
