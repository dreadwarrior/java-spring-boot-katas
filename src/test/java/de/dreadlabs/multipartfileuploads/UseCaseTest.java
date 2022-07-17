package de.dreadlabs.multipartfileuploads;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UseCaseTest {

    private final UserDetailsManager userDetailsManager;
    private final TestRestTemplate httpClient;

    public UseCaseTest(@Autowired UserDetailsManager userDetailsManager, @Autowired TestRestTemplate httpClient) {
        this.userDetailsManager = userDetailsManager;
        this.httpClient = httpClient;
    }

    @BeforeEach
    void removeJohn() {
        userDetailsManager.deleteUser("john");
    }

    @Nested
    class GivenJohnIsARegisteredUser {

        @BeforeEach
        void registerUser() {
            userDetailsManager.createUser(User.builder().username("john").password("{noop}doe").roles("USER").build());
        }

        @Nested
        class WhenHeUploadsASingleFile {

            private ResponseEntity<String> response;

            @BeforeEach
            void uploadSingleFile() {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("files", new ClassPathResource("multipartfileuploads/file1mb"));

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                response = httpClient.withBasicAuth("john", "doe")
                        .exchange(
                                "/multipartfileuploads",
                                HttpMethod.POST,
                                new HttpEntity<>(body, httpHeaders),
                                String.class
                        );
            }

            @Test
            void thenItsSizeIsDisplayed() {
                assertThat(response).extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                                .containsExactly(HttpStatus.OK, "1048576");
            }
        }

        @Nested
        class WhenHeUploadsMultipleFiles {

            private ResponseEntity<String> response;

            @BeforeEach
            void uploadTwoFiles() {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("files", new ClassPathResource("multipartfileuploads/file1mb"));
                body.add("files", new ClassPathResource("multipartfileuploads/file512kb"));

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                response = httpClient.withBasicAuth("john", "doe")
                        .exchange(
                                "/multipartfileuploads",
                                HttpMethod.POST,
                                new HttpEntity<>(body, httpHeaders),
                                String.class
                        );
            }

            @Test
            void thenTheirSizesAssociatedWithTheirNamesAreDisplayed() {
                assertThat(response).extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                        .containsExactly(HttpStatus.OK, "file1mb: 1048576, file512kb: 524288");
            }
        }

        @Nested
        class WhenHeUploadsNothing {

            private ResponseEntity<String> response;

            @BeforeEach
            void uploadTwoFiles() {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                response = httpClient.withBasicAuth("john", "doe")
                        .exchange(
                                "/multipartfileuploads",
                                HttpMethod.POST,
                                new HttpEntity<>(body, httpHeaders),
                                String.class
                        );
            }

            @Test
            void thenANotificationIsDisplayed() {
                assertThat(response).extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                        .containsExactly(HttpStatus.OK, "No files uploaded.");
            }
        }

        @Nested
        class WhenHeUploadsASingleFileBeingTooLarge {

            private ResponseEntity<String> response;

            @BeforeEach
            void uploadSingleFile() {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("files", new ClassPathResource("multipartfileuploads/filegt1mb"));

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                response = httpClient.withBasicAuth("john", "doe")
                        .exchange(
                                "/multipartfileuploads",
                                HttpMethod.POST,
                                new HttpEntity<>(body, httpHeaders),
                                String.class
                        );
            }

            @Test
            void thenTheUploadIsRejected() {
                assertThat(response).extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                        .containsExactly(HttpStatus.BAD_REQUEST, "An uploaded file is too large.");
            }
        }

        @Nested
        class WhenHeUploadsMultipleFilesWhoseTotalSizeIsEqualToTheRequestLimit {

            private ResponseEntity<String> response;

            @BeforeEach
            void uploadMultipleFilesWithinFileSizeLimits() {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("files", new ClassPathResource("multipartfileuploads/file1mb"));
                body.add("files", new ClassPathResource("multipartfileuploads/file1mb"));

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                response = httpClient.withBasicAuth("john", "doe")
                        .exchange(
                                "/multipartfileuploads",
                                HttpMethod.POST,
                                new HttpEntity<>(body, httpHeaders),
                                String.class
                        );
            }

            @Test
            void thenTheUploadIsRejected() {
                assertThat(response).extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                        .containsExactly(HttpStatus.BAD_REQUEST, "The total size of all uploaded files is too large.");
            }
        }

    }
}