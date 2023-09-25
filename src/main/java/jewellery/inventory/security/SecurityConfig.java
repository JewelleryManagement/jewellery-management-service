package jewellery.inventory.security;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ApplicationContext applicationContext;

  private static final String USERS_ENDPOINT = "/users";
  private static final String AUTH_ENDPOINT = "/auth/**";

  @Value("${cors.allowedOrigins}")
  private String[] allowedOrigins;

  @Value("${cors.allowedMethods}")
  private String[] allowedMethods;

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
    configureHttpSecurity(http, jwtAuthenticationEntryPoint);
    return http.build();
  }

  private void configureHttpSecurity(
      HttpSecurity http, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(Customizer.withDefaults())
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    AuthorizeHttpRequestsConfigurer<HttpSecurity> authorizeRequests =
        new AuthorizeHttpRequestsConfigurer<>(applicationContext);
    authorizeRequests
        .getRegistry()
        .requestMatchers(HttpMethod.POST, USERS_ENDPOINT)
        .permitAll()
        .requestMatchers(AUTH_ENDPOINT)
        .permitAll()
        .anyRequest()
        .authenticated();

    http.apply(authorizeRequests);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    configuration.setAllowedMethods(Arrays.asList(allowedMethods));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
