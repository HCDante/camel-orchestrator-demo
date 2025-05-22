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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    void postCargoDevuelve200yOK() {
        String url = "http://localhost:" + port + "/api/transactions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummy-token");

        String body = """
            {
              "tipo": "cargo",
              "cuenta": "12345",
              "monto": 250.0
            }
            """;

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // Verificamos status y payload exacto
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"status\":\"OK\"}");

        // Verificamos que se llamó a publish(...)
        Mockito.verify(eventPublisher).publish(Mockito.any());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EventPublisher eventPublisher() {
            return Mockito.mock(EventPublisher.class);
        }
    }
}
