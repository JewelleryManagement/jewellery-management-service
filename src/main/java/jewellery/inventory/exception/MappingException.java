package jewellery.inventory.exception;

public class MappingException extends RuntimeException {
  public MappingException(Object resourceRequestDto) {
    super(String.format("Can't map: %s", resourceRequestDto));
  }
}
