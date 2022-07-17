package de.dreadlabs.greetings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UseCaseTest {

    private final UserDetailsManager userDetailsManager;
    private final TestRestTemplate httpClient;

    public UseCaseTest(@Autowired UserDetailsManager userDetailsManager, @Autowired TestRestTemplate httpClient) {
        this.userDetailsManager = userDetailsManager;
        this.httpClient = httpClient;
    }

    @Nested
    class GivenJaneIsARegisteredUser {

        @BeforeEach
        void registerUser() {
            userDetailsManager.createUser(User.builder().username("jane").password("{noop}doe").roles("USER").build());
        }

        @Nested
        class WhenSheRequestsAGreetingForJohn {

            private String response;

            @BeforeEach
            void requestGreeting() {
                response = httpClient.withBasicAuth("jane", "doe").getForObject("/greetings/John", String.class);
            }

            @Test
            void thenAFriendlyGreetingIsDisplayed() {
                assertThat(response).isEqualTo("Hello, John!");
            }
        }
    }
}