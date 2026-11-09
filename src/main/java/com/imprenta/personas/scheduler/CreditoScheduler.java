package com.imprenta.personas.scheduler;

import com.imprenta.personas.repository.AlertaCreditoRepository;
import com.imprenta.personas.repository.AlertaCreditoRepository.AlertaVencimiento;
import com.imprenta.personas.service.NotificacionCreditoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CreditoScheduler {

    private static final Logger log = LoggerFactory.getLogger(CreditoScheduler.class);

    private final AlertaCreditoRepository alertaCreditoRepository;
    private final NotificacionCreditoService notificacionService;

    public CreditoScheduler(AlertaCreditoRepository alertaCreditoRepository, 
                            NotificacionCreditoService notificacionService) {
        this.alertaCreditoRepository = alertaCreditoRepository;
        this.notificacionService = notificacionService;
    }

    @Scheduled(cron = "0 0 8 * * ?") 
    public void evaluarVencimientosDeCredito() {
        log.info("Iniciando evaluación de órdenes con crédito vencido...");
        LocalDate hoy = LocalDate.now();

        // 1. Notificación PREVENTIVA (Vence mañana)
        procesarAlertas(hoy.plusDays(1), 
            "Recordatorio de Pago", 
            "La orden #%s vence mañana. Tiene un saldo pendiente de pago.");

        // 2. Notificación DÍA CERO (Vence hoy)
        procesarAlertas(hoy, 
            "Crédito por Vencer", 
            "La fecha límite para pagar la orden #%s es hoy. Por favor regularizar saldo.");

        // 3. Notificación POST-VENCIMIENTO (Venció ayer)
        procesarAlertas(hoy.minusDays(1), 
            "Alerta: Orden Vencida", 
            "El crédito de la orden #%s expiró ayer y presenta saldo pendiente.");
            
        log.info("Evaluación de créditos finalizada.");
    }

    private void procesarAlertas(LocalDate fechaEvaluacion, String titulo, String plantillaMensaje) {
        List<AlertaVencimiento> alertas = alertaCreditoRepository.buscarOrdenesPorVencer(fechaEvaluacion);
        
        for (AlertaVencimiento alerta : alertas) {
            String mensaje = String.format(plantillaMensaje, alerta.getIdOrden());
            
            notificacionService.notificarVencimientoACobranza(
                List.of("CONTADORA", "DUEÑO"), 
                titulo + " (Cliente " + alerta.getIdCliente() + ")", 
                mensaje
            );
        }
    }
}