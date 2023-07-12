package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import jewellery.inventory.dto.PreciousMetalDTO;
import jewellery.inventory.services.PreciousMetalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/preciousMetals")
@RequiredArgsConstructor
public class PreciousMetalController {
  private final PreciousMetalService preciousMetalService;

  @GetMapping
  public ResponseEntity<List<PreciousMetalDTO>> getAllPreciousMetal() {
    return ResponseEntity.ok(preciousMetalService.getAllPreciousMetal());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PreciousMetalDTO> getPreciousMetalById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(preciousMetalService.getPreciousMetalById(id));
  }

  @PostMapping
  public ResponseEntity<PreciousMetalDTO> createPreciousMetal(
      @RequestBody @Valid PreciousMetalDTO preciousMetalDTO,
      UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/api/v1/preciousMetals/{id}")
            .buildAndExpand(preciousMetalService.createPreciousMetal(preciousMetalDTO).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<PreciousMetalDTO> updatePreciousMetal(
      @PathVariable("id") Long id, @Valid @RequestBody PreciousMetalDTO preciousMetalDTO) {
    return ResponseEntity.ok(preciousMetalService.updatePreciousMetal(id, preciousMetalDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deletePreciousMetalById(@PathVariable("id") Long id) {
    preciousMetalService.deletePreciousMetalById(id);
    return new ResponseEntity<>(
        "Precious metal with id: " + id + " has been deleted successfully!", HttpStatus.OK);
  }
}
