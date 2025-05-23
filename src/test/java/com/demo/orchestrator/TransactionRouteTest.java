//package com.demo.orchestrator;
//
//import com.demo.orchestrator.model.TransactionRequest;
//import com.demo.orchestrator.routes.TransactionRoute;
//import com.demo.orchestrator.service.EventPublisher;
//import org.apache.camel.CamelContext;
//import org.apache.camel.Exchange;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.http.HttpComponent;               // **ADDED**
//import org.apache.camel.test.junit5.CamelTestSupport;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.Map;
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//
//public class TransactionRouteTest extends CamelTestSupport {
//
//    // Mock inyectado en el registro de Camel
//    private final EventPublisher eventPublisher = Mockito.mock(EventPublisher.class);
//
//    @Override
//    protected CamelContext createCamelContext() throws Exception {
//        CamelContext context = super.createCamelContext();
//        // Registrar mock para que @Autowired en la ruta lo encuentre
//        context.getRegistry().bind("eventPublisher", eventPublisher);
//        // Registrar componente http4 para health-check
//        context.addComponent("http4", new HttpComponent());
//        return context;
//    }
//
//    @Override
//    protected RouteBuilder createRouteBuilder() {
//        // Cargar la ruta que vamos a probar
//        return new TransactionRoute();
//    }
//
//    @Test
//    void testProcessTransactionCargo() {
//        // Preparar el request
//        TransactionRequest request = new TransactionRequest();
//        request.setTipo("cargo");
//        request.setCuenta("12345");
//        request.setMonto(250.0);
//
//        // Enviar al endpoint directo
//        Exchange exchange = template.request("direct:processTransaction", ex -> {
//            ex.getIn().setBody(request);
//            ex.getIn().setHeader("Authorization", "Bearer dummy-token");
//        });
//
//        // Recuperar cuerpo como Map
//        @SuppressWarnings("unchecked")
//        Map<String, Object> body = exchange.getMessage().getBody(Map.class);
//
//        // Validar contenido
//        assertEquals("OK", body.get("status"));
//
//        // Verificar que se llamó al EventPublisher
//        Mockito.verify(eventPublisher).publish(any(TransactionRequest.class));
//    }
//}
