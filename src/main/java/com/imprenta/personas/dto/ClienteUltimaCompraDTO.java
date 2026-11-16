package com.imprenta.personas.dto;

import java.time.LocalDateTime;

public interface ClienteUltimaCompraDTO {
    Integer getIdCliente();

    String getNombre();

    String getRfc();

    String getTelefono();

    Integer getIdUltimaOrden();

    LocalDateTime getFechaUltimaCompra();
}