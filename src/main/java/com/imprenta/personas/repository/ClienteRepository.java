package com.imprenta.personas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.imprenta.personas.dto.ClienteUltimaCompraDTO;
import com.imprenta.personas.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByRfc(String rfc);

    boolean existsByEmail(String email);

    Cliente findByEmail(String email);

    List<Cliente> findByNombreContainingIgnoreCaseOrRazonSocialContainingIgnoreCase(String nombre, String razon);

    boolean existsByEmailAndIdClienteNot(String email, Integer idCliente);

    boolean existsByRfcAndIdClienteNot(String rfc, Integer idCliente);

    List<Cliente> findByRfcIsNull();

    List<Cliente> findByRfcIsNotNull();

    @Query("SELECT c FROM Cliente c WHERE c.rfc IS NULL OR TRIM(c.rfc) = ''")
    List<Cliente> findClientesMostrador();

    @Query("SELECT c FROM Cliente c WHERE c.rfc IS NOT NULL AND TRIM(c.rfc) != ''")
    List<Cliente> findClientesFactura();

    @Query(value = """
            SELECT
                c.id_cliente AS idCliente,
                c.nombre AS nombre,
                c.rfc AS rfc,
                c.telefono AS telefono,
                o.id_orden AS idUltimaOrden,
                o.fecha_creacion AS fechaUltimaCompra
            FROM clientes c
            LEFT JOIN (
                SELECT id_orden, id_cliente, fecha_creacion
                FROM ordenes
                WHERE id_cliente = :idCliente
                ORDER BY fecha_creacion DESC
                LIMIT 1
            ) o ON c.id_cliente = o.id_cliente
            WHERE c.id_cliente = :idCliente
            """, nativeQuery = true)
    Optional<ClienteUltimaCompraDTO> findByIdConUltimaCompra(@Param("idCliente") Integer idCliente);

}