package com.imprenta.personas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imprenta.personas.scheduler.CreditoScheduler;

@RestController
@RequestMapping("/api/test")
public class TestSchedulerController {

    private final CreditoScheduler creditoScheduler;

    public TestSchedulerController(CreditoScheduler creditoScheduler) {
        this.creditoScheduler = creditoScheduler;
    }

    @GetMapping("/disparar-vencimientos")
    public ResponseEntity<String> probarVencimientos() {
        creditoScheduler.evaluarVencimientosDeCredito();
        return ResponseEntity.ok("Proceso de evaluación de créditos ejecutado. Revisa tu consola y WebSockets.");
    }
}