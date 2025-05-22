package com.demo.orchestrator.service;

import com.demo.orchestrator.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(EventPublisher.class);

    public void publish(TransactionRequest req) {
        // Simulación de publicación a Kafka y almacenamiento en MongoDB
        LOG.info("[EVENT] Publicando transacción: tipo={}, cuenta={}, monto={}",
                req.getTipo(), req.getCuenta(), req.getMonto());
        // Aquí iría la integración real a MongoDB y Kafka
    }
}
