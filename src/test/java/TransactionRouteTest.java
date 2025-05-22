//import com.demo.orchestrator.OrchestratorApplication;
//import com.demo.orchestrator.model.TransactionRequest;
//import org.apache.camel.CamelContext;
//import org.apache.camel.ProducerTemplate;
//import org.apache.camel.builder.NotifyBuilder;
//import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@CamelSpringBootTest
//@SpringBootTest(classes = OrchestratorApplication.class)
//public class TransactionRouteTest {
//
//    @Autowired
//    private CamelContext camelContext;
//
//    @Autowired
//    private ProducerTemplate producerTemplate;
//
//    @Test
//    void testCargoTransaction() {
//        TransactionRequest req = new TransactionRequest();
//        req.setTipo("cargo");
//        req.setCuenta("12345");
//        req.setMonto(100);
//
//        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();
//        Object response = producerTemplate.requestBody("direct:process-transaction", req);
//
//        Assertions.assertTrue(notify.matches(2, java.util.concurrent.TimeUnit.SECONDS));
//        Assertions.assertNotNull(response);
//        Assertions.assertTrue(response.toString().contains("OK"));
//    }
//
//    @Test
//    void testAbonoTransaction() {
//        TransactionRequest req = new TransactionRequest();
//        req.setTipo("abono");
//        req.setCuenta("67890");
//        req.setMonto(200);
//
//        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();
//        Object response = producerTemplate.requestBody("direct:process-transaction", req);
//
//        Assertions.assertTrue(notify.matches(2, java.util.concurrent.TimeUnit.SECONDS));
//        Assertions.assertNotNull(response);
//        Assertions.assertTrue(response.toString().contains("OK"));
//    }
//}
