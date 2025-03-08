package at.ac.tuwien.sepr.assignment.individual.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration that effectively disables restrictions for cross-origin requests.
 * This configuration is active in all profiles except "prod" and is useful during development.
 * <b>Warning:</b> Disabling CORS in production can lead to security vulnerabilities.
 */
@Profile("!prod")
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Configures CORS to allow all origins and HTTP methods.
   *
   * @param registry the {@link CorsRegistry} to configure
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "POST", "OPTIONS", "HEAD", "DELETE", "PUT", "PATCH");
  }
}
