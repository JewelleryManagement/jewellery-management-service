package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import jewellery.inventory.dto.LinkingPartDTO;
import jewellery.inventory.services.LinkingPartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/linkingParts")
@RequiredArgsConstructor
public class LinkingPartController {
  private final LinkingPartService linkingPartService;

  @GetMapping
  public ResponseEntity<List<LinkingPartDTO>> getAllLinkingPart() {
    return ResponseEntity.ok(linkingPartService.getAllLinkingPart());
  }

  @GetMapping("/{id}")
  public ResponseEntity<LinkingPartDTO> getLinkingPartById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(linkingPartService.getLinkingPartById(id));
  }

  @PostMapping
  public ResponseEntity<LinkingPartDTO> createLinkingPart(
      @RequestBody @Valid LinkingPartDTO linkingPartDTO,
      UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/api/v1/linkingPart/{id}")
            .buildAndExpand(linkingPartService.createLinkingPart(linkingPartDTO).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<LinkingPartDTO> updateLinkingPart(
      @PathVariable("id") Long id, @Valid @RequestBody LinkingPartDTO linkingPartDTO) {
    return ResponseEntity.ok(linkingPartService.updateLinkingPart(id, linkingPartDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteLinkingPartById(@PathVariable("id") Long id) {
    linkingPartService.deleteLinkingPartById(id);
    return new ResponseEntity<>(
        "LinkingPart with id: " + id + " has been deleted successfully!!", HttpStatus.OK);
  }
}
