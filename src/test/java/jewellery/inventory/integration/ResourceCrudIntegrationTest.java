package jewellery.inventory.integration;

import static jewellery.inventory.util.TestUtil.provideResourceDtos;
import static jewellery.inventory.util.TestUtil.provideUpdatedResourceDtos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.ResourceDTO;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ResourceCrudIntegrationTest {

  private String getBaseResourceUrl() {
    return "http://localhost:" + port + "/resources";
  }

  @Value(value = "${local.server.port}")
  private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired TestRestTemplate testRestTemplate;

  @AfterEach
  void deleteResources() throws JsonProcessingException {
    List<ResourceDTO> resourceDtosFromDb = getResourcesWithRequest();
    resourceDtosFromDb.forEach(
            resourceDTO -> testRestTemplate.delete(getBaseResourceUrl() + "/" + resourceDTO.getId()));
  }

  @Test
  void greetingShouldReturnDefaultMessage() {
    assertThat(
            this.testRestTemplate.getForObject("http://localhost:" + port + "/home", String.class))
        .contains("Hello world");
  }

  @Test
  void willAddResourceToDatabase() throws JsonProcessingException {
    List<ResourceDTO> inputDtos = getResourceDtosWithNullIds();

    sendCreateRequestsFor(inputDtos);

    List<ResourceDTO> createdDtos = getResourcesWithRequest();
    inputDtos = zipWith(inputDtos, getIds(createdDtos));
    assertEquals(inputDtos, createdDtos);
  }

  @Test
  void willUpdateResourceToDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(getResourceDtosWithNullIds());
    List<ResourceDTO> createdDtos = getResourcesWithRequest();
    List<ResourceDTO> updatedInputDtos = getUpdatedResourceDtosWithNullIds();

    sendUpdateRequestsFor(updatedInputDtos, getIds(createdDtos));

    updatedInputDtos = zipWith(updatedInputDtos, getIds(createdDtos));
    assertEquals(updatedInputDtos, getResourcesWithRequest());
  }

  @NotNull
  private static List<UUID> getIds(List<ResourceDTO> dtos) {
    return dtos.stream().map(ResourceDTO::getId).toList();
  }

  @NotNull
  private List<ResourceDTO> getResourcesWithRequest() throws JsonProcessingException {
    String response = this.testRestTemplate.getForObject(getBaseResourceUrl(), String.class);
    List<ResourceDTO> responseDtos = objectMapper.readValue(response, new TypeReference<>() {});
    return responseDtos;
  }

  @NotNull
  private static List<ResourceDTO> getUpdatedResourceDtosWithNullIds() {
    List<ResourceDTO> inputDtos = provideUpdatedResourceDtos().toList();
    inputDtos.forEach(resourceDTO -> resourceDTO.setId(null));
    return inputDtos;
  }

  @NotNull
  private static List<ResourceDTO> getResourceDtosWithNullIds() {
    List<ResourceDTO> inputDtos = provideResourceDtos().toList();
    inputDtos.forEach(resourceDTO -> resourceDTO.setId(null));
    return inputDtos;
  }

  private void sendCreateRequestsFor(List<ResourceDTO> inputDtos) {
    inputDtos.forEach(
        resourceDTO -> {
          ResponseEntity<ResourceDTO> responseEntity =
              this.testRestTemplate.postForEntity(
                  getBaseResourceUrl(), resourceDTO, ResourceDTO.class);
          assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        });
  }

  private void sendUpdateRequestsFor(
      List<ResourceDTO> updatedResourceDtosWithNullIds, List<UUID> ids) {
    zipWith(updatedResourceDtosWithNullIds, ids);
    updatedResourceDtosWithNullIds.forEach(
        resourceDTO -> this.testRestTemplate.put(getBaseResourceUrl() + "/", ResourceDTO.class));
  }

  @NotNull
  private List<ResourceDTO> zipWith(
      List<ResourceDTO> updatedResourceDtosWithNullIds, List<UUID> ids) {
    return StreamUtils.zip(
            updatedResourceDtosWithNullIds.stream(),
            ids.stream(),
            (updatedResourceDto, id) -> {
              updatedResourceDto.setId(id);
              return updatedResourceDto;
            })
        .toList();
  }
}
