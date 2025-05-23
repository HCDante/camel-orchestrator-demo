package com.demo.orchestrator;

import com.demo.orchestrator.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {
                OrchestratorApplication.class,
                TransactionHttpTest.TestConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration"
        }
)
public class TransactionHttpTest {


    // 1. Configuración de puerto
    @LocalServerPort
    private int port;

    // 2. Cliente REST para pruebas HTTP
    @Autowired
    private TestRestTemplate restTemplate;

    // 3. Mock de publicación de eventos
    @Autowired
    private EventPublisher eventPublisher;

    // 4. Caso de prueba: POST /transactions
    @Test
    void postCargoDevuelve200yOK() {
        // --- Construir URL completa ---
        String url = "http://localhost:" + port + "/api/transactions";

        // --- Cabeceras HTTP ---
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummy-token");  // Simular JWT

        // --- Cuerpo de la petición JSON ---
        String body = """
            { "tipo": "cargo", "cuenta": "12345", "monto": 250.0 }
            """;

        // --- Envolver cabeceras y cuerpo en HttpEntity ---
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // --- Enviar petición y recibir respuesta ---
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // --- Validar código HTTP 200 ---
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // --- Validar cuerpo exacto ---
        assertThat(response.getBody()).isEqualTo("{\"status\":\"OK\"}");

        // --- Verificar que el eventPublisher.publish(...) fue invocado ---
        Mockito.verify(eventPublisher).publish(Mockito.any());
    }

    // 5. Configuración de beans para pruebas
    @TestConfiguration
    static class TestConfig {
        @Bean
        public EventPublisher eventPublisher() {
            // Crear mock para verificar invocaciones
            return Mockito.mock(EventPublisher.class);
        }
    }
}
