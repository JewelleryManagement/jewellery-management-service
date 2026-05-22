package jewellery.inventory.exception.role;


public class RoleAlreadyAssignedException extends RuntimeException {
  public RoleAlreadyAssignedException() {
    super("Role is assigned to members and cannot be deleted");
  }
}
