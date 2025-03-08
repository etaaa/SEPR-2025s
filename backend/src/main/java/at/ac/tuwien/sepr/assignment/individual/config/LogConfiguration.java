package at.ac.tuwien.sepr.assignment.individual.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Configuration class for request logging.
 * Registers a filter to log incoming HTTP requests.
 */
@Configuration
public class LogConfiguration {

  /**
   * Registers the {@link LogFilter} to log HTTP requests.
   *
   * @return a configured {@link FilterRegistrationBean} for logging
   */
  @Bean
  public FilterRegistrationBean<OncePerRequestFilter> logFilter() {
    var reg = new FilterRegistrationBean<OncePerRequestFilter>(new LogFilter());
    reg.addUrlPatterns("/*");
    reg.setName("logFilter");
    reg.setOrder(Ordered.LOWEST_PRECEDENCE);
    return reg;
  }
}
