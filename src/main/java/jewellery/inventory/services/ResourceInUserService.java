package jewellery.inventory.services;

import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.ResourceInUserDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.ResourceInUser;
import jewellery.inventory.repositories.ResourceInUserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceInUserService {
  private final ResourceInUserRepository resourceInUserRepository;
  private final ModelMapper modelMapper;

  public ResourceInUserDTO resourceInUserToDTO(ResourceInUser resourceInUser) {
    return modelMapper.map(resourceInUser, ResourceInUserDTO.class);
  }

  public ResourceInUser dtoToResourceInUser(ResourceInUserDTO resourceInUserDTO) {
    return modelMapper.map(resourceInUserDTO, ResourceInUser.class);
  }

  public List<ResourceInUserDTO> getAllResourcesInUser() {
    List<ResourceInUser> resourceInUsers = resourceInUserRepository.findAll();
    return resourceInUsers.stream().map(this::resourceInUserToDTO).toList();
  }

  public ResourceInUserDTO getResourceInUserById(Long id) {
    Optional<ResourceInUser> resourceInUser = resourceInUserRepository.findById(id);
    if (resourceInUser.isEmpty()) {
      throw new ApiRequestException("ResourceInUser not found for id " + id);
    }
    return resourceInUserToDTO(resourceInUser.get());
  }

  public ResourceInUserDTO createResourceInUser(ResourceInUserDTO resourceInUserDTO) {
    ResourceInUser resourceInUser = dtoToResourceInUser(resourceInUserDTO);
    resourceInUserRepository.save(resourceInUser);
    return resourceInUserToDTO(resourceInUser);
  }

  public ResourceInUserDTO updateResourceInUser(Long id, ResourceInUserDTO resourceInUserDTO) {
    Optional<ResourceInUser> findResourceInUser = resourceInUserRepository.findById(id);
    if (findResourceInUser.isEmpty()) {
      throw new ApiRequestException("ResourceInUser not found for id " + id);
    }

    //        findResourceInUser.get().setOwner(resourceInUserDTO.getOwner());
    //        findResourceInUser.get().setResource(resourceInUserDTO.getResource());
    findResourceInUser.get().setQuantity(resourceInUserDTO.getQuantity());

    resourceInUserRepository.save(findResourceInUser.get());
    return resourceInUserToDTO(findResourceInUser.get());
  }

  public void deleteResourceInUserById(Long id) {
    Optional<ResourceInUser> resourceInUser = resourceInUserRepository.findById(id);
    if (resourceInUser.isEmpty()) {
      throw new ApiRequestException("ResourceInUser not found for id " + id);
    }

    resourceInUserRepository.delete(resourceInUser.get());
  }
}
