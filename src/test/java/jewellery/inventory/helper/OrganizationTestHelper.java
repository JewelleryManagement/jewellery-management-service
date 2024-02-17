package jewellery.inventory.helper;

import jewellery.inventory.model.Organization;
import jewellery.inventory.model.UserInOrganization;

import java.util.List;
import java.util.UUID;

public class OrganizationTestHelper {

    public static Organization getTestOrganization(){
        Organization organization=new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Name");
        organization.setAddress("Test Address");
        organization.setNote("Test Note");
        return organization;
    }
}
