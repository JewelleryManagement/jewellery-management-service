package jewellery.inventory.services;

<<<<<<< Updated upstream
import jewellery.inventory.DTO.GemstoneDTO;
=======
import jewellery.inventory.dto.GemstoneDTO;
>>>>>>> Stashed changes
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.Gemstone;
import jewellery.inventory.repositories.GemstoneRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GemstoneService {
    private GemstoneRepository gemstoneRepository;
    private final ModelMapper modelMapper;
    public GemstoneDTO gemstoneToGemstoneDTO(Gemstone gemstone){
        return modelMapper.map(gemstone, GemstoneDTO.class);
    }
    public Gemstone gemstoneDTOtoGemstone(GemstoneDTO gemstoneDTO){
        return modelMapper.map(gemstoneDTO, Gemstone.class);
    }

    public List<GemstoneDTO> getAllGemstone() {
        List<Gemstone> gemstones = gemstoneRepository.findAll();
        return gemstones
                .stream()
                .map(this::gemstoneToGemstoneDTO)
                .toList();
    }

    public GemstoneDTO createGemstone(GemstoneDTO gemstoneDTO) {
        gemstoneRepository.save(gemstoneDTOtoGemstone(gemstoneDTO));
        return gemstoneDTO;
    }

    public GemstoneDTO getGemstoneById(Long id) {
        Optional<Gemstone> gemstone = gemstoneRepository.findById(id);
        if (gemstone.isEmpty()) {
            throw new ApiRequestException("This gemstone is not found");
        }
        return gemstoneToGemstoneDTO(gemstone.get());
    }

    public void deleteGemstoneById(Long id) {
        Optional<Gemstone> gemstone = gemstoneRepository.findById(id);
        if (gemstone.isEmpty()) {
            throw new ApiRequestException("Gemstone not found for id " + id);
        }
        gemstoneRepository.delete(gemstone.get());
    }

    public GemstoneDTO updateGemstone(Long id, GemstoneDTO gemstoneDTO) {
        Optional<Gemstone> findGemstone = gemstoneRepository.findById(id);
        if (findGemstone.isEmpty()) {
            throw new ApiRequestException("Gemstone not found");
        }
        findGemstone.get().setColor(gemstoneDTO.getColor());
        findGemstone.get().setCarat(gemstoneDTO.getCarat());
        findGemstone.get().setCut(gemstoneDTO.getCut());
        findGemstone.get().setClarity(gemstoneDTO.getClarity());
        findGemstone.get().setDimensionX(gemstoneDTO.getDimensionX());
        findGemstone.get().setDimensionY(gemstoneDTO.getDimensionY());
        findGemstone.get().setDimensionZ(gemstoneDTO.getDimensionZ());
        findGemstone.get().setShape(gemstoneDTO.getShape());

        gemstoneRepository.save(findGemstone.get());
        return gemstoneToGemstoneDTO(findGemstone.get());
    }
}
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
