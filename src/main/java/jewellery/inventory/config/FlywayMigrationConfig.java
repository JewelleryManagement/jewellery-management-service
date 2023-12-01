package jewellery.inventory.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayMigrationConfig {

  private static final String LOCATION = "db/migration/dev";

  @Autowired
  public FlywayMigrationConfig(DataSource dataSource) {
    Flyway.configure()
        .baselineOnMigrate(true)
        .validateMigrationNaming(true)
        .dataSource(dataSource)
        .locations(LOCATION)
        .load()
        .migrate();
  }
}
