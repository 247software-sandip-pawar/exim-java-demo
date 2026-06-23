package com.eximplatform.documents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Documents microservice: generates trade documents (proforma/commercial invoice, packing list,
 * etc.) for an order (owns exim_documents). The order is read from the orders service over REST.
 *
 * <p>Real PDF rendering and object storage are deferred (see EXECUTION_PLAN Phase 4): a generated
 * document records a {@code fileUrl} placeholder, mirroring verification's KYC fileUrl approach.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class DocumentsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentsServiceApplication.class, args);
    }
}
