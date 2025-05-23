package com.demo.orchestrator.routes;

import com.demo.orchestrator.model.TransactionRequest;
import com.demo.orchestrator.service.EventPublisher;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.http.base.HttpOperationFailedException;
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

        // 0. Configurar Dead Letter Channel global
        errorHandler(deadLetterChannel("log:deadLetter")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000)
                .retryAttemptedLogLevel(LoggingLevel.WARN));

        // 1. onException específicos
        onException(ValidationException.class)
                .handled(true)
                .log(LoggingLevel.WARN, "Payload inválido: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("{\"error\":\"Formato inválido\"}"));

        onException(HttpOperationFailedException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "Llamada HTTP fallida: código ${exception.statusCode}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
                .setBody(simple("{\"error\":\"Servicio externo no disponible\"}"));

//        onException(MongoException.class)
//                .handled(true)
//                .log(LoggingLevel.ERROR, "Error MongoDB: ${exception.message}")
//                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
//                .setBody(simple("{\"error\":\"Problema de base de datos\"}"));

        // OnException genérico
        onException(Exception.class)
                .handled(true)
                .log("Error inesperado: ${exception.message}")
                // Usamos la cabecera estándar para el código HTTP
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("{\"error\":\"${exception.message}\"}"));

        // 2. Ruta periódica de heartbeat y health-check
        from("timer://orchestratorHeartBeat?fixedRate=true&period=60000")
                .routeId("heartbeatRoute")
                .log("🔔 Heartbeat fired at ${header.firedTime}")
                .to("direct:healthCheck");

        from("direct:healthCheck")
                .routeId("healthCheckRoute")
                .to("http://localhost:8080/actuator/health")
                .log("Health status: ${body}");


        // 3. Configuración REST
        restConfiguration()
                .component("servlet")
                .contextPath("/api")
                .bindingMode(RestBindingMode.json)
                // JSON con sangría para facilitar lectura durante desarrollo
                .dataFormatProperty("prettyPrint", "false");

        // 4. Endpoint de Prueba (/test/a)
        rest("/test")
                .get("/a")
                .type(TransactionRequest.class)
                .to("direct:testPing");

        from("direct:testPing")
                // Ping a servicio externo para validación de configuración
                .to("https://postman-echo.com/get?bridgeEndpoint=true&id=123");

        // 5. Endpoint Principal (/transactions)
        rest("/transactions")
                .post()
                .type(TransactionRequest.class)
                .to("direct:processTransaction");

        from("direct:processTransaction")
                .routeId("transactionRoute")

                // 5.1 Logging de la solicitud
                .log("Solicitud recibida: ${body}")

                // 5.2 Seguridad (Validación JWT simulada)
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

                // 5.3 Lógica de negocio: cargo / abono
                .choice()
                .when(simple("${body.tipo} == 'cargo'"))
                .log("Realizando cargo de ${body.monto} a la cuenta ${body.cuenta}")
                .when(simple("${body.tipo} == 'abono'"))
                .log("Realizando abono de ${body.monto} a la cuenta ${body.cuenta}")
                .otherwise()
                .throwException(new IllegalArgumentException("Tipo de transacción desconocido"))
                .end()

                // 5.4 Publicación de evento (simulación de guardar en MongoDB y publicar evento Kafka)
                .process(exchange -> {
                    TransactionRequest request = exchange.getIn().getBody(TransactionRequest.class);
                    eventPublisher.publish(request);
                })

                // 6.5 Construcción de la respuesta
                .setBody(constant(Collections.singletonMap("status", "OK")))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}