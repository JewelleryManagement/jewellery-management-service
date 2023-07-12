package jewellery.inventory.controller;

import jakarta.validation.Valid;

import jewellery.inventory.dto.GemstoneDTO;

import jewellery.inventory.services.GemstoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gemstones")
@RequiredArgsConstructor
public class GemstoneController {
    private final GemstoneService gemstoneService;

    @GetMapping
    public ResponseEntity<List<GemstoneDTO>> getAllGemstone() {
        return ResponseEntity.ok(gemstoneService.getAllGemstone());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GemstoneDTO> getGemstoneById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(gemstoneService.getGemstoneById(id));
    }

    @PostMapping
    public ResponseEntity<GemstoneDTO> createGemstone(@RequestBody @Valid GemstoneDTO gemstoneDTO, UriComponentsBuilder uriComponentsBuilder) {
        URI location = uriComponentsBuilder.path("/api/v1/gemstone/{id}")
                .buildAndExpand(gemstoneService.createGemstone(gemstoneDTO).getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<GemstoneDTO> updateGemstone(@PathVariable("id") Long id, @Valid @RequestBody GemstoneDTO gemstoneDTO) {
        return ResponseEntity.ok(gemstoneService.updateGemstone(id, gemstoneDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGemstoneById(@PathVariable("id") Long id) {
        gemstoneService.deleteGemstoneById(id);
        return new ResponseEntity<>("Gemstone with id: " + id + " has been deleted successfully!!", HttpStatus.OK);
    }

}