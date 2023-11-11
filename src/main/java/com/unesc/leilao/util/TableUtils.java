package com.unesc.leilao.util;

import com.unesc.leilao.proto.Produto;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TableUtils {
    public static final String EMPTY_CELL_VALUE = "---";

    public static final NumberFormat currency = NumberFormat.getCurrencyInstance();

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static final Object[] PRODUTO_COLUMNS = new Object[] {
            "Identificador",
            "Descrição",
            "Data do cadastro",
            "Lance mínimo",
            "Último lance",
            "Usuário",
            "Data do lance"
    };

    public static Object[] getProdutoColumns(Produto produto) {
        boolean hasUltimoLance = produto.hasUltimoLance();

        return new Object[] {
                produto.getId(),
                produto.getDescricao(),
                dateTimeFormatter.format(LocalDateTime.parse(produto.getDatetime())),
                currency.format(produto.getValorMinimo()),
                hasUltimoLance
                        ? currency.format(produto.getUltimoLance().getValor()) : EMPTY_CELL_VALUE,
                hasUltimoLance
                        ? produto.getUltimoLance().getUsuario() : EMPTY_CELL_VALUE,
                hasUltimoLance
                        ? dateTimeFormatter.format(LocalDateTime.parse(produto.getUltimoLance().getDatetime()))
                        : EMPTY_CELL_VALUE,
        };
    }
}
