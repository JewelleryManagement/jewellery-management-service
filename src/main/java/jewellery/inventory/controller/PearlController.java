package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import jewellery.inventory.dto.PearlDTO;
import jewellery.inventory.services.PearlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/pearls")
@RequiredArgsConstructor
public class PearlController {
  private final PearlService pearlService;

  @GetMapping
  public ResponseEntity<List<PearlDTO>> getAllPearl() {
    return ResponseEntity.ok(pearlService.getAllPearl());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PearlDTO> getPearlById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(pearlService.getPearlById(id));
  }

  @PostMapping
  public ResponseEntity<PearlDTO> createPearl(
      @RequestBody @Valid PearlDTO gemstoneDTO, UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/api/v1/pearl/{id}")
            .buildAndExpand(pearlService.createPearl(gemstoneDTO).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<PearlDTO> updatePearl(
      @PathVariable("id") Long id, @Valid @RequestBody PearlDTO pearlDTO) {
    return ResponseEntity.ok(pearlService.updatePearl(id, pearlDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deletePearlById(@PathVariable("id") Long id) {
    pearlService.deletePearlById(id);
    return new ResponseEntity<>(
        "Pearl with id: " + id + " has been deleted successfully!!", HttpStatus.OK);
  }
}
