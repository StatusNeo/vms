package com.statusneo.vms.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationGreenMailExtensionTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP).withConfiguration(GreenMailConfiguration.aConfig().withUser("duke", "springboot")).withPerMethodLifecycle(false); // share a GreenMail server for all test methods of a test class

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldSendEmailWithCorrectPayloadToUser() throws Exception {

        String payload = "{ \"email\": \"duke@spring.io\", \"content\": \"Hello World!\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = this.testRestTemplate.postForEntity("/notifications", request, Void.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {

            MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
            assertEquals("Hello World!", GreenMailUtil.getBody(receivedMessage));
            assertEquals(1, receivedMessage.getAllRecipients().length);
            assertEquals("duke@spring.io", receivedMessage.getAllRecipients()[0].toString());
        });

    }

}

