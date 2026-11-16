package com.imprenta.personas.dto;

public class CambiarPasswordDTO {
    private String passwordActual;
    private String nuevaPassword;

    public CambiarPasswordDTO() {

    }

    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

}