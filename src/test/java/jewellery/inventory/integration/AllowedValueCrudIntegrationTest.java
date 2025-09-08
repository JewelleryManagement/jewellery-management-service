package jewellery.inventory.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import jewellery.inventory.dto.response.resource.AllowedValueResponseDto;
import jewellery.inventory.dto.request.resource.AllowedValueRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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

        AllowedValueRequestDto requestDto = new AllowedValueRequestDto("Metal", "color", "gold");
        ResponseEntity<String> postResponse = testRestTemplate.postForEntity(baseUrl, requestDto, String.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String url = baseUrl + "?resourceClazz=Metal&fieldName=color";
        ResponseEntity<String> getResponse = testRestTemplate.getForEntity(url, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<AllowedValueResponseDto> values = objectMapper.readValue(getResponse.getBody(), new TypeReference<>() {});
        assertThat(values).extracting(AllowedValueResponseDto::getValue).contains("gold");
    }

    @Test
    void canDeleteAllowedValue() {

        AllowedValueRequestDto requestDto = new AllowedValueRequestDto("Metal", "color", "silver");
        testRestTemplate.postForEntity(baseUrl, requestDto, String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AllowedValueRequestDto> request = new HttpEntity<>(requestDto, headers);
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(baseUrl, HttpMethod.DELETE, request, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String url = baseUrl + "?resourceClazz=Metal&fieldName=color";
        ResponseEntity<String> getResponse = testRestTemplate.getForEntity(url, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Should not contain the deleted value
        assertThat(getResponse.getBody()).doesNotContain("silver");
    }

    @Test
    void deleteNonExistentAllowedValueReturns404() {
        AllowedValueRequestDto nonExistentDto = new AllowedValueRequestDto("NonExistent", "field", "value");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AllowedValueRequestDto> request = new HttpEntity<>(nonExistentDto, headers);
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(baseUrl, HttpMethod.DELETE, request, Void.class);
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
} 