package com.demo.orchestrator.model;

/**
 * DTO para representar la petición de una transacción.
 * Incluye el tipo de operación (cargo/abono), la cuenta afectada y el monto a procesar.
 */

public class TransactionRequest {
    // Tipo: "cargo" o "abono"
    private String tipo;
    // Cuenta afectada
    private String cuenta;
    // Monto a procesar
    private double monto;

    // Constructor vacío para JSON
    public TransactionRequest() {}

    // Getters y setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCuenta() { return cuenta; }
    public void setCuenta(String cuenta) { this.cuenta = cuenta; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
}
