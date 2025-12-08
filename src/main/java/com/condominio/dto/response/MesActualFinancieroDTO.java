package com.condominio.dto.response;

import lombok.Data;

@Data
public class MesActualFinancieroDTO {
    private int entradas;
    private int salidas;
    private int balance;
    private int saldoActual;


    public void calcularBalance() {
        int balanceCalculado = 0;
        balanceCalculado = this.entradas - this.salidas;
        this.balance = balanceCalculado;
    }
}
