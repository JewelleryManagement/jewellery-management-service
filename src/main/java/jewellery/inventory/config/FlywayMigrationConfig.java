package jewellery.inventory.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayMigrationConfig {

  public FlywayMigrationConfig(
      DataSource dataSource,
      @Value("${flyway.migration.location}") String[] flywayMigrationLocation) {
    Flyway.configure()
        .baselineOnMigrate(true)
        .validateMigrationNaming(true)
        .dataSource(dataSource)
        .locations(flywayMigrationLocation)
        .load()
        .migrate();
  }
}
