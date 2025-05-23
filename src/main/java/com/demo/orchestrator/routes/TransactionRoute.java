package com.demo.orchestrator.routes;

import com.demo.orchestrator.model.TransactionRequest;
import com.demo.orchestrator.service.EventPublisher;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
/**
 * Definiciòn de endpoints REST para transacciones de cargo y abono,
 * ping de prueba, manejo de excepciones, JWT simulado,
 * lógica de negocio, publicación de eventos y construcción de respuestas.
 */

@Component
public class TransactionRoute extends RouteBuilder {

    @Autowired
    private EventPublisher eventPublisher;

    @Override
    public void configure() throws Exception {

        // 1. Onservabilidad, manejo de Excepciones
        onException(Exception.class)
                .handled(true)
                .log("Error: ${exception.message}")
                // Usamos la cabecera estándar para el código HTTP
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody()
                .simple("{\"error\":\"${exception.message}\"}");

        // 2. Configuración REST
        restConfiguration()
                .component("servlet")
                .contextPath("/api")
                .bindingMode(RestBindingMode.json)
                // JSON con sangría para facilitar lectura durante desarrollo
                .dataFormatProperty("prettyPrint", "false");

        // 3. Endpoint de Prueba (/test/a)
        rest("/test")
                .get("/a")
                .type(TransactionRequest.class)
                .to("direct:testPing");

        from("direct:testPing")
                // Ping a servicio externo para validación de configuración
                .to("https://postman-echo.com/get?bridgeEndpoint=true&id=123");

        // 4. Endpoint Principal (/transactions)
        rest("/transactions")
                .post()
                .type(TransactionRequest.class)
                .to("direct:processTransaction");

        from("direct:processTransaction")
                .routeId("transactionRoute")

                // 4.1 Logging de la solicitud
                .log("Solicitud recibida: ${body}")

                // 4.2 Seguridad (Validación JWT simulada)
                .process(exchange -> {
                    String authHeader = exchange.getIn().getHeader("Authorization", String.class);
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        throw new RuntimeException("Falta token de autorización");
                    }
                    String token = authHeader.substring(7);
                    if (token.isBlank()) {
                        throw new RuntimeException("Token inválido");
                    }
                })

                // 4.3 Lógica de negocio: cargo / abono
                .choice()
                .when(simple("${body.tipo} == 'cargo'"))
                .log("Realizando cargo de ${body.monto} a la cuenta ${body.cuenta}")
                .when(simple("${body.tipo} == 'abono'"))
                .log("Realizando abono de ${body.monto} a la cuenta ${body.cuenta}")
                .otherwise()
                .throwException(new IllegalArgumentException("Tipo de transacción desconocido"))
                .end()

                // 4.4 Publicación de evento (simulación de guardar en MongoDB y publicar evento Kafka)
                .process(exchange -> {
                    TransactionRequest request = exchange.getIn().getBody(TransactionRequest.class);
                    eventPublisher.publish(request);
                })

                // 4.5 Construcción de la respuesta
                .setBody(constant(Collections.singletonMap("status", "OK")))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}