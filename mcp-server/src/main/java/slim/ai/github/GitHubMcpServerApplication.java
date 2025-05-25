package slim.ai.github;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import slim.ai.github.adapter.mcp.GitHubToolHandler;

@SpringBootApplication
public class GitHubMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubMcpServerApplication.class, args);
    }
    
    @Bean
    public ToolCallbackProvider githubTools(GitHubToolHandler handler) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(handler)
                .build();
    }

    /**
     * @see ReactiveOAuth2ClientConfiguration.SecurityWebFilterChainConfiguration
     */
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        var corsConfig = corsConfigurationSource();
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.configurationSource(corsConfig));
        http.authorizeExchange((exchange) -> {
            exchange.pathMatchers(".well-known/**").permitAll();
            exchange.anyExchange().authenticated();
        });
        http.oauth2Login(withDefaults());
        http.oauth2Client(withDefaults());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:6274"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
