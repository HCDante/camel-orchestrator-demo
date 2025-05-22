package com.demo.orchestrator.model;

public class TransactionRequest {
    private String tipo; // "cargo" o "abono"
    private String cuenta;
    private double monto;

    // Getters y Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCuenta() { return cuenta; }
    public void setCuenta(String cuenta) { this.cuenta = cuenta; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
}
