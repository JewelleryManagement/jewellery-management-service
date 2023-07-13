package jewellery.inventory.services;

import jewellery.inventory.dto.ResourceDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.Resource;
import jewellery.inventory.repositories.ResourceRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ResourceService {
    @Autowired
    private final ModelMapper resourceMapper;
    @Autowired
    private ResourceRepository resourceRepository;

    public ResourceDTO map(Resource resource) {
        return resourceMapper.map(resource, ResourceDTO.class);
    }

    public Resource map(ResourceDTO resourceDTO) {
        return resourceMapper.map(resourceDTO, Resource.class);
    }

    public List<ResourceDTO> getAllResource() {
        List<Resource> resources = resourceRepository.findAll();
        return resources.stream().map(this::map).toList();
    }

    public ResourceDTO createResource(ResourceDTO resourceDTO) {
        resourceRepository.save(map(resourceDTO));
        return resourceDTO;
    }

    public ResourceDTO getResourceById(UUID id) {
        Optional<Resource> resource = resourceRepository.findById(id);
        if (resource.isEmpty()) {
            throw new ApiRequestException("This resource is not found");
        }
        return map(resource.get());
    }

    public void deleteResourceById(UUID id) {
        Optional<Resource> resource = resourceRepository.findById(id);
        if (resource.isEmpty()) {
            throw new ApiRequestException("Resource not found for id " + id);
        }
        resourceRepository.delete(resource.get());
    }

    public ResourceDTO updateResource(UUID id, ResourceDTO resourceDTO) {
        Optional<Resource> findResource = resourceRepository.findById(id);
        if (findResource.isEmpty()) {
            throw new ApiRequestException("Resource not found");
        }
        findResource.get().setName(resourceDTO.getName());
        findResource.get().setQuantityType(resourceDTO.getQuantityType());

        resourceRepository.save(findResource.get());
        return map(findResource.get());
    }
}
