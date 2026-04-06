package jewellery.inventory.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jewellery.inventory.model.Permission;

@Converter
public class PermissionConverter implements AttributeConverter<Permission, String> {

  @Override
  public String convertToDatabaseColumn(Permission attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public Permission convertToEntityAttribute(String dbData) {
    return dbData == null ? null : Permission.fromValue(dbData);
  }
}
