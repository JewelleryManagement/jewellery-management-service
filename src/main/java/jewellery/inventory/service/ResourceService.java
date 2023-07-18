package jewellery.inventory.service;

import jewellery.inventory.dto.ResourceDTO;
import jewellery.inventory.exeption.ApiRequestException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ResourceService {
    @Autowired
    private ResourceRepository resourceRepository;

    private ResourceDTO map(Resource resource) {
        return ResourceMapper.map(resource);
    }

    private Resource map(ResourceDTO resourceDTO) {
        return ResourceMapper.map(resourceDTO);
    }

    public List<ResourceDTO> getAllResource() {
        List<Resource> resources = resourceRepository.findAll();
        return resources.stream().map(this::map).toList();
    }

    public ResourceDTO createResource(ResourceDTO resourceDTO) {
        return map(resourceRepository.save(map(resourceDTO)));
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

        Resource toUpdate = ResourceMapper.map(resourceDTO);
        toUpdate.setId(id);
        return map(resourceRepository.save(toUpdate));
    }
}
