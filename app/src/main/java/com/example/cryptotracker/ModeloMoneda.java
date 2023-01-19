package com.example.cryptotracker;

public class ModeloMoneda {

    private String id;
    private String simbolo;
    private String nombre;
    private String logo;
    private String precioUsd;
    private String cambio24h;
    private String cambio7d;

    public ModeloMoneda(){

    }

    public ModeloMoneda(String id, String simbolo, String nombre, String icono, String precioUsd, String cambio24h, String cambio7d) {
        this.id = id;
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.logo = icono;
        this.precioUsd = precioUsd;
        this.cambio24h = cambio24h;
        this.cambio7d = cambio7d;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPrecioUsd() {
        return precioUsd;
    }

    public void setPrecioUsd(String precioUsd) {
        this.precioUsd = precioUsd;
    }

    public String getCambio24h() {
        return cambio24h;
    }

    public void setCambio24h(String cambio24h) {
        this.cambio24h = cambio24h;
    }

    public String getCambio7d() {
        return cambio7d;
    }

    public void setCambio7d(String cambio7d) {
        this.cambio7d = cambio7d;
    }
}
