package jewellery.inventory.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jewellery.inventory.model.resource.AllowedValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class AllowedValueCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

    private final String baseUrl = "/allowed-values";

    @AfterEach
    void cleanup() {
        // Optionally implement cleanup if needed
    }

    @Test
    void canAddAndFetchAllowedValue() throws Exception {
        AllowedValue.AllowedValueId id = AllowedValue.AllowedValueId.builder()
                .resourceClazz("Metal")
                .fieldName("color")
                .value("gold")
                .build();
        AllowedValue allowedValue = AllowedValue.builder().id(id).build();

        ResponseEntity<AllowedValue> postResponse = testRestTemplate.postForEntity(baseUrl, allowedValue, AllowedValue.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody().getId()).isEqualTo(id);

        String url = baseUrl + "?resourceClazz=Metal&fieldName=color";
        ResponseEntity<String> getResponse = testRestTemplate.getForEntity(url, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<AllowedValue> values = objectMapper.readValue(getResponse.getBody(), new TypeReference<>() {});
        assertThat(values).extracting(v -> v.getId().getValue()).contains("gold");
    }

    @Test
    void canDeleteAllowedValue() {
        AllowedValue.AllowedValueId id = AllowedValue.AllowedValueId.builder()
                .resourceClazz("Metal")
                .fieldName("color")
                .value("silver")
                .build();
        AllowedValue allowedValue = AllowedValue.builder().id(id).build();
        testRestTemplate.postForEntity(baseUrl, allowedValue, AllowedValue.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AllowedValue.AllowedValueId> request = new HttpEntity<>(id, headers);
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(baseUrl, HttpMethod.DELETE, request, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String url = baseUrl + "?resourceClazz=Metal&fieldName=color";
        ResponseEntity<String> getResponse = testRestTemplate.getForEntity(url, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Should not contain the deleted value
        assertThat(getResponse.getBody()).doesNotContain("silver");
    }
} 