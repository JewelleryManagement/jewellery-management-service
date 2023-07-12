package jewellery.inventory.services;

import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.LinkingPartDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.LinkingPart;
import jewellery.inventory.repositories.LinkingPartRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LinkingPartService {
  private final ModelMapper modelMapper;
  private LinkingPartRepository linkingPartRepository;

  public LinkingPartDTO linkingPartToLinkingPartDTO(LinkingPart linkingPart) {
    return modelMapper.map(linkingPart, LinkingPartDTO.class);
  }

  public LinkingPart linkingPartDTOtoLinkingPart(LinkingPartDTO linkingPartDTO) {
    return modelMapper.map(linkingPartDTO, LinkingPart.class);
  }

  public List<LinkingPartDTO> getAllLinkingPart() {
    List<LinkingPart> linkingParts = linkingPartRepository.findAll();
    return linkingParts.stream().map(this::linkingPartToLinkingPartDTO).toList();
  }

  public LinkingPartDTO createLinkingPart(LinkingPartDTO linkingPartDTO) {
    linkingPartRepository.save(linkingPartDTOtoLinkingPart(linkingPartDTO));
    return linkingPartDTO;
  }

  public LinkingPartDTO getLinkingPartById(Long id) {
    Optional<LinkingPart> linkingPart = linkingPartRepository.findById(id);
    if (linkingPart.isEmpty()) {
      throw new ApiRequestException("This linkingPart is not found");
    }
    return linkingPartToLinkingPartDTO(linkingPart.get());
  }

  public void deleteLinkingPartById(Long id) {
    Optional<LinkingPart> linkingPart = linkingPartRepository.findById(id);
    if (linkingPart.isEmpty()) {
      throw new ApiRequestException("LinkingPart not found for id " + id);
    }
    linkingPartRepository.delete(linkingPart.get());
  }

  public LinkingPartDTO updateLinkingPart(Long id, LinkingPartDTO linkingPartDTO) {
    Optional<LinkingPart> findLinkingPart = linkingPartRepository.findById(id);
    if (findLinkingPart.isEmpty()) {
      throw new ApiRequestException("LinkingPart not found");
    }
    findLinkingPart.get().setName(linkingPartDTO.getName());
    findLinkingPart.get().setQuantityType(linkingPartDTO.getQuantityType());
    findLinkingPart.get().setDescription(linkingPartDTO.getDescription());

    linkingPartRepository.save(findLinkingPart.get());
    return linkingPartToLinkingPartDTO(findLinkingPart.get());
  }
}
