package jewellery.inventory.aspect;

// import java.util.HashMap;
// import java.util.Map;
// import jewellery.inventory.dto.response.UserResponseDto;
// import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
// import jewellery.inventory.dto.response.resource.LinkingPartResponseDto;
// import jewellery.inventory.dto.response.resource.PearlResponseDto;
// import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
// import jewellery.inventory.model.User;
// import jewellery.inventory.model.resource.Gemstone;
// import jewellery.inventory.model.resource.LinkingPart;
// import jewellery.inventory.model.resource.Pearl;
// import jewellery.inventory.model.resource.PreciousMetal;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class EntityMappingConfig {
  //  @Bean
  //  public Map<Class<?>, String> entityNameMapping() {
  //    Map<Class<?>, String> map = new HashMap<>();
  //    map.put(UserResponseDto.class, "User");
  //    map.put(GemstoneResponseDto.class, "Gemstone");
  //    map.put(PearlResponseDto.class, "Pearl");
  //    map.put(PreciousMetalResponseDto.class, "PreciousMetal");
  //    map.put(LinkingPartResponseDto.class, "LinkingPart");
  //    return map;
  //  }
  //
  //  @Bean
  //  public String getEntityName(Class<?> entityClass) {
  //    return switch (entityClass.getSimpleName()) {
  //      case "UserResponseDto" -> "User";
  //      case "GemstoneResponseDto" -> "Gemstone";
  //      case "PearlResponseDto" -> "Pearl";
  //      case "PreciousMetalResponseDto" -> "PreciousMetal";
  //      case "LinkingPart" -> "LinkingPartResponseDto";
  //      default -> throw new IllegalArgumentException("Unknown entity class: " + entityClass);
  //    };
  //  }
  //
  //  @Bean
  //  public Map<Class<?>, Class<?>> entityDtoMapping() {
  //    Map<Class<?>, Class<?>> map = new HashMap<>();
  //    map.put(User.class, UserResponseDto.class);
  //    map.put(Gemstone.class, GemstoneResponseDto.class);
  //    map.put(Pearl.class, PearlResponseDto.class);
  //    map.put(LinkingPart.class, LinkingPartResponseDto.class);
  //    map.put(PreciousMetal.class, PreciousMetalResponseDto.class);
  //    return map;
  //  }
  //
  //  @Bean
  //  public Class<?> getDtoClass(Class<?> entityClass) {
  //    return switch (entityClass.getSimpleName()) {
  //      case "User" -> UserResponseDto.class;
  //      case "Gemstone" -> GemstoneResponseDto.class;
  //      case "Pearl" -> PearlResponseDto.class;
  //      case "LinkingPart" -> LinkingPartResponseDto.class;
  //      case "PreciousMetal" -> PreciousMetalResponseDto.class;
  //      default -> throw new IllegalArgumentException("Unknown entity class: " + entityClass);
  //    };
  //  }
// }
