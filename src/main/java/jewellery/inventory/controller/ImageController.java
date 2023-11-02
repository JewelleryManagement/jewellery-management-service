package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Upload new image")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    ImageResponseDto uploadImage(@RequestParam("image") @Valid MultipartFile file) throws IOException {
        return imageService.uploadImageToFileSystem(file);
    }

    @Operation(summary = "Get image")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    byte[] getImage(@PathVariable @Valid String fileName) throws IOException {
        return imageService.downloadImageFormFileSystem(fileName);
    }
}
