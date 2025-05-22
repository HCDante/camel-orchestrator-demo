package com.demo.orchestrator.routes;

import com.demo.orchestrator.model.TransactionRequest;
import com.demo.orchestrator.service.EventPublisher;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionRoute extends RouteBuilder {

    @Autowired
    private EventPublisher eventPublisher;

    @Override
    public void configure() throws Exception {

        // Observabilidad - Logging estructurado
        onException(Exception.class)
            .handled(true)
            .log("Error: ${exception.message}")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
            .setBody().simple("{\"error\":\"${exception.message}\"}");

        rest("/api/transactions")
            .post()
            .type(TransactionRequest.class)
            .to("direct:process-transaction");

        from("direct:process-transaction")
            .routeId("transactionRoute")
            .log("Solicitud recibida: ${body}")
            // Seguridad (simulada): Validar JWT desde cabecera Authorization
            .process(exchange -> {
                String authHeader = exchange.getIn().getHeader("Authorization", String.class);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new RuntimeException("Falta token de autorización");
                }
                // Simulación de validación JWT (solo validación sintáctica)
                String token = authHeader.substring(7);
                if (token.isBlank()) throw new RuntimeException("Token inválido");
            })
            // Lógica de flujo cargo/abono
            .choice()
                .when(simple("${body.tipo} == 'cargo'"))
                    .log("Realizando cargo de ${body.monto} a la cuenta ${body.cuenta}")
                .when(simple("${body.tipo} == 'abono'"))
                    .log("Realizando abono de ${body.monto} a la cuenta ${body.cuenta}")
                .otherwise()
                    .throwException(new IllegalArgumentException("Tipo de transacción desconocido"))
            .end()
            // Simulación de guardar en MongoDB y publicar evento Kafka
            .process(exchange -> {
                TransactionRequest req = exchange.getIn().getBody(TransactionRequest.class);
                eventPublisher.publish(req); // Simulado, imprime en logs
            })
            .setBody(simple("{\"status\":\"OK\"}"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}
