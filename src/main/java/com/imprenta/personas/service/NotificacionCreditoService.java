package com.imprenta.personas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imprenta.personas.config.MqttGateway;
import com.imprenta.personas.dto.NotificacionPayloadDTO;
import com.imprenta.personas.model.Notificacion;
import com.imprenta.personas.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionCreditoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionCreditoService.class);

    private final NotificacionRepository notificacionRepository;
    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.default-topic}")
    private String defaultTopic;

    public NotificacionCreditoService(NotificacionRepository notificacionRepository,
                                      MqttGateway mqttGateway,
                                      ObjectMapper objectMapper) {
        this.notificacionRepository = notificacionRepository;
        this.mqttGateway = mqttGateway;
        this.objectMapper = objectMapper;
    }

    // Método principal que llamarás cuando el crédito venza
    @Transactional
    public void notificarVencimientoCredito(String receptorId, String titulo, String mensaje) {
        // Notificar al cliente específico
        this.procesarYNotificar("sistema", receptorId, "CREDITO_VENCIDO", titulo, mensaje);
    }
    
    // Método por si también necesitas notificar al área de cobranza
    @Transactional
    public void notificarVencimientoACobranza(List<String> rolesCobranza, String titulo, String mensaje) {
        List<String> idsDestinatarios = notificacionRepository.findIdsUsuariosPorRoles(rolesCobranza);

        if (idsDestinatarios == null || idsDestinatarios.isEmpty()) {
            log.warn("No se encontraron usuarios para los roles de cobranza: {}", rolesCobranza);
            return;
        }

        for (String receptorId : idsDestinatarios) {
            this.procesarYNotificar("sistema", receptorId, "ALERTA_COBRANZA", titulo, mensaje);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarYNotificar(String emisorId, String receptorId, String categoria, String titulo, String mensaje) {

        // 1. Guardar en Base de Datos
        Notificacion nuevaNotificacion = new Notificacion();
        nuevaNotificacion.setEmisorId(emisorId);
        nuevaNotificacion.setReceptorId(receptorId);
        nuevaNotificacion.setCategoria(categoria);
        nuevaNotificacion.setTitulo(titulo);
        nuevaNotificacion.setMensaje(mensaje);
        nuevaNotificacion.setLeida(false);

        notificacionRepository.save(nuevaNotificacion);

        // 2. Construir Payload
        NotificacionPayloadDTO.Contenido contenido = new NotificacionPayloadDTO.Contenido();
        contenido.setTitulo(titulo);
        contenido.setMensaje(mensaje);

        NotificacionPayloadDTO payload = new NotificacionPayloadDTO();
        payload.setEmisorId(emisorId);
        payload.setReceptorId(receptorId);
        payload.setCategoria(categoria);
        payload.setContenido(contenido);

        // 3. Enviar por MQTT
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            mqttGateway.sendMqtt(jsonPayload, defaultTopic);
            log.info("Notificación de crédito guardada y enviada por MQTT a la cola: {}", defaultTopic);
        } catch (Exception e) {
            log.error("No se pudo publicar la notificación de crédito en MQTT (tópico: {}). "
                    + "La notificación sí quedó guardada en BD.", defaultTopic, e);
        }
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerPendientes(String receptorId) {
        return notificacionRepository.findByReceptorIdAndLeidaFalseOrderByCreadaEnDesc(receptorId);
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación con ID " + notificacionId + " no encontrada"));

        notificacion.setLeida(true);
        notificacion.setLeidaEn(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }
}