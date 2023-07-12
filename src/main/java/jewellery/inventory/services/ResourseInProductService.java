package jewellery.inventory.services;

import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.ResourceInProductDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.ResourceInProduct;
import jewellery.inventory.repositories.ResourceInProductRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceInProductService {
  private final ModelMapper modelMapper;
  private ResourceInProductRepository resourceInProductRepository;

  public ResourceInProductDTO resourceInProductToResourceInProductDTO(
      ResourceInProduct resourceInProduct) {
    return modelMapper.map(resourceInProduct, ResourceInProductDTO.class);
  }

  public ResourceInProduct resourceInProductDTOtoResourceInProduct(
      ResourceInProductDTO resourceInProductDTO) {
    return modelMapper.map(resourceInProductDTO, ResourceInProduct.class);
  }

  public List<ResourceInProductDTO> getAllResourceInProduct() {
    List<ResourceInProduct> resourceInProducts = resourceInProductRepository.findAll();
    return resourceInProducts.stream().map(this::resourceInProductToResourceInProductDTO).toList();
  }

  public ResourceInProductDTO createResourceInProduct(ResourceInProductDTO resourceInProductDTO) {
    resourceInProductRepository.save(resourceInProductDTOtoResourceInProduct(resourceInProductDTO));
    return resourceInProductDTO;
  }

  public ResourceInProductDTO getResourceInProductById(Long id) {
    Optional<ResourceInProduct> resourceInProduct = resourceInProductRepository.findById(id);
    if (resourceInProduct.isEmpty()) {
      throw new ApiRequestException("This resource in product is not found");
    }
    return resourceInProductToResourceInProductDTO(resourceInProduct.get());
  }

  public void deleteResourceInProductById(Long id) {
    Optional<ResourceInProduct> resourceInProduct = resourceInProductRepository.findById(id);
    if (resourceInProduct.isEmpty()) {
      throw new ApiRequestException("Resource in product not found for id " + id);
    }
    resourceInProductRepository.delete(resourceInProduct.get());
  }

  public ResourceInProductDTO updateResourceInProduct(
      Long id, ResourceInProductDTO resourceInProductDTO) {
    Optional<ResourceInProduct> findResourceInProduct = resourceInProductRepository.findById(id);
    if (findResourceInProduct.isEmpty()) {
      throw new ApiRequestException("Resource in product not found");
    }
    //        findResourceInProduct.get().setResource(resourceInProductDTO.getResource());
    findResourceInProduct.get().setQuantity(resourceInProductDTO.getQuantity());
    //        findResourceInProduct.get().setProduct(resourceInProductDTO.getProduct());

    resourceInProductRepository.save(findResourceInProduct.get());
    return resourceInProductToResourceInProductDTO(findResourceInProduct.get());
  }
}
