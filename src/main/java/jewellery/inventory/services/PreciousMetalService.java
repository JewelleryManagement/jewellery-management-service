package jewellery.inventory.services;

import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.PreciousMetalDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.PreciousMetal;
import jewellery.inventory.repositories.PreciousMetalRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PreciousMetalService {
  private final ModelMapper modelMapper;
  private PreciousMetalRepository preciousMetalRepository;

  public PreciousMetalDTO preciousMetalToPreciousMetalDTO(PreciousMetal preciousMetal) {
    return modelMapper.map(preciousMetal, PreciousMetalDTO.class);
  }

  public PreciousMetal preciousMetalDTOtoPreciousMetal(PreciousMetalDTO preciousMetalDTO) {
    return modelMapper.map(preciousMetalDTO, PreciousMetal.class);
  }

  public List<PreciousMetalDTO> getAllPreciousMetal() {
    List<PreciousMetal> preciousMetals = preciousMetalRepository.findAll();
    return preciousMetals.stream().map(this::preciousMetalToPreciousMetalDTO).toList();
  }

  public PreciousMetalDTO createPreciousMetal(PreciousMetalDTO preciousMetalDTO) {
    preciousMetalRepository.save(preciousMetalDTOtoPreciousMetal(preciousMetalDTO));
    return preciousMetalDTO;
  }

  public PreciousMetalDTO getPreciousMetalById(Long id) {
    Optional<PreciousMetal> preciousMetal = preciousMetalRepository.findById(id);
    if (preciousMetal.isEmpty()) {
      throw new ApiRequestException("This precious metal is not found");
    }
    return preciousMetalToPreciousMetalDTO(preciousMetal.get());
  }

  public void deletePreciousMetalById(Long id) {
    Optional<PreciousMetal> preciousMetal = preciousMetalRepository.findById(id);
    if (preciousMetal.isEmpty()) {
      throw new ApiRequestException("Precious metal not found for id " + id);
    }
    preciousMetalRepository.delete(preciousMetal.get());
  }

  public PreciousMetalDTO updatePreciousMetal(Long id, PreciousMetalDTO preciousMetalDTO) {
    Optional<PreciousMetal> findPreciousMetal = preciousMetalRepository.findById(id);
    if (findPreciousMetal.isEmpty()) {
      throw new ApiRequestException("Precious metal not found");
    }
    findPreciousMetal.get().setName(preciousMetalDTO.getName());
    findPreciousMetal.get().setQuantityType(preciousMetalDTO.getQuantityType());
    findPreciousMetal.get().setType(preciousMetalDTO.getType());
    findPreciousMetal.get().setPurity(preciousMetalDTO.getPurity());
    findPreciousMetal.get().setColor(preciousMetalDTO.getColor());
    findPreciousMetal.get().setPlating(preciousMetalDTO.getPlating());

    preciousMetalRepository.save(findPreciousMetal.get());
    return preciousMetalToPreciousMetalDTO(findPreciousMetal.get());
  }
}
