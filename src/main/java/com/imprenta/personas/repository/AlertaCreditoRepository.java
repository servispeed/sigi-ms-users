package com.imprenta.personas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.imprenta.personas.model.Cliente; 

import java.time.LocalDate;
import java.util.List;

public interface AlertaCreditoRepository extends JpaRepository<Cliente, Integer> {

    interface AlertaVencimiento {
        String getIdCliente();
        String getIdOrden();
        Double getMontoTotal();
        Double getMontoPagado();
    }

    // Calcula dinámicamente sumando los "dias_credito" a la "fecha_creacion"
    // y filtra solo aquellas órdenes donde aún hay un saldo pendiente.
    @Query(value = "SELECT CAST(c.id_cliente AS VARCHAR) AS idCliente, " +
                   "CAST(o.id_orden AS VARCHAR) AS idOrden, " +
                   "CAST(o.monto_total AS DOUBLE PRECISION) AS montoTotal, " +
                   "CAST(o.monto_pagado AS DOUBLE PRECISION) AS montoPagado " +
                   "FROM ordenes o " +
                   "INNER JOIN clientes c ON o.id_cliente = c.id_cliente " +
                   "WHERE c.credito_habilitado = true " +
                   "AND o.monto_pagado < o.monto_total " +
                   "AND CAST(o.fecha_creacion AS DATE) + c.dias_credito = :fechaEvaluacion", 
           nativeQuery = true)
    List<AlertaVencimiento> buscarOrdenesPorVencer(@Param("fechaEvaluacion") LocalDate fechaEvaluacion);
}