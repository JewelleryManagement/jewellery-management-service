package jewellery.inventory.integration;

import static jewellery.inventory.util.TestUtil.provideResourceDtos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import jewellery.inventory.dto.ResourceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ResourceCrudIntegrationTest {

  @Value(value = "${local.server.port}")
  private int port;

  private String getBaseResourceUrl() {
    return "http://localhost:" + port + "/resources";
  }

  @Autowired TestRestTemplate testRestTemplate;

  @Test
  void greetingShouldReturnDefaultMessage() {
    assertThat(
            this.testRestTemplate.getForObject("http://localhost:" + port + "/home", String.class))
        .contains("Hello world");
  }

  @Test
  void willAddResourceToDatabase() {
    provideResourceDtos()
        .forEach(
            resourceDTO ->
                this.testRestTemplate
                    .postForEntity(getBaseResourceUrl(), resourceDTO, ResourceDTO.class)
                    .getStatusCode()
                    .is2xxSuccessful());
    String response = this.testRestTemplate.getForObject(getBaseResourceUrl(), String.class);

    assertTrue(response.contains("Pearl"));
    assertTrue(response.contains("Gemstone"));
    assertTrue(response.contains("PreciousMetal"));
    assertTrue(response.contains("LinkingPart"));
  }

  @Test
  void willGetResourcesFromDatabase() {
    assertThat(this.testRestTemplate.getForObject(getBaseResourceUrl(), String.class)).isNotEmpty();
  }
}
