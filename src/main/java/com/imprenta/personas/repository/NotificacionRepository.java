package com.imprenta.personas.repository;

import com.imprenta.personas.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByReceptorIdAndLeidaFalseOrderByCreadaEnDesc(String receptorId);

    @Query(value = "SELECT u.id_usuario FROM usuarios u " +
            "INNER JOIN cat_roles r ON u.id_rol = r.id_rol " +
            "WHERE UPPER(r.descripcion) IN (:roles)", nativeQuery = true)
    List<String> findIdsUsuariosPorRoles(@Param("roles") List<String> roles);
}