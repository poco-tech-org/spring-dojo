package com.example.blog.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationAndLoginIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void integrationTest() {
        // ## Arrange ##

        // ## Act ##
        var responseSpec = webTestClient.get().uri("/").exchange();

        // ## Assert ##
        var response = responseSpec.returnResult(String.class);
        var xsrfTokenOpt = Optional.ofNullable(response.getResponseCookies().getFirst("XSRF-TOKEN"));

        responseSpec.expectStatus().isNoContent();
        assertThat(xsrfTokenOpt)
                .isPresent()
                .hasValueSatisfying(xsrfTokenCookie ->
                        assertThat(xsrfTokenCookie.getValue()).isNotBlank()
                );
    }
}
