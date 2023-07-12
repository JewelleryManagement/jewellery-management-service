package jewellery.inventory.services;

import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.PearlDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.Pearl;
import jewellery.inventory.repositories.PearlRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PearlService {
  private final ModelMapper modelMapper;
  private PearlRepository pearlRepository;

  public PearlDTO pearlToPearlDTO(Pearl pearl) {
    return modelMapper.map(pearl, PearlDTO.class);
  }

  public Pearl pearlDTOtoPearl(PearlDTO pearlDTO) {
    return modelMapper.map(pearlDTO, Pearl.class);
  }

  public List<PearlDTO> getAllPearl() {
    List<Pearl> pearls = pearlRepository.findAll();
    return pearls.stream().map(this::pearlToPearlDTO).toList();
  }

  public PearlDTO createPearl(PearlDTO pearlDTO) {
    pearlRepository.save(pearlDTOtoPearl(pearlDTO));
    return pearlDTO;
  }

  public PearlDTO getPearlById(Long id) {
    Optional<Pearl> pearl = pearlRepository.findById(id);
    if (pearl.isEmpty()) {
      throw new ApiRequestException("This pearl is not found");
    }
    return pearlToPearlDTO(pearl.get());
  }

  public void deletePearlById(Long id) {
    Optional<Pearl> pearl = pearlRepository.findById(id);
    if (pearl.isEmpty()) {
      throw new ApiRequestException("Pearl not found for id " + id);
    }
    pearlRepository.delete(pearl.get());
  }

  public PearlDTO updatePearl(Long id, PearlDTO pearlDTO) {
    Optional<Pearl> findPearl = pearlRepository.findById(id);
    if (findPearl.isEmpty()) {
      throw new ApiRequestException("Pearl not found");
    }
    findPearl.get().setName(pearlDTO.getName());
    findPearl.get().setQuantityType(pearlDTO.getQuantityType());
    findPearl.get().setType(pearlDTO.getType());
    findPearl.get().setSize(pearlDTO.getSize());
    findPearl.get().setQuality(pearlDTO.getQuality());
    findPearl.get().setColor(pearlDTO.getColor());
    findPearl.get().setShape(pearlDTO.getShape());

    pearlRepository.save(findPearl.get());
    return pearlToPearlDTO(findPearl.get());
  }
}
