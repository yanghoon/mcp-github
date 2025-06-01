package slim.ai.github;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;
import slim.ai.github.adapter.mcp.GitHubToolHandler;

@SpringBootApplication
@Slf4j
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
     * @see OAuth2AuthorizationServerWebSecurityConfiguration
     */
    @Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var authorizationServer = OAuth2AuthorizationServerConfigurer.authorizationServer();
		http.securityMatcher(authorizationServer.getEndpointsMatcher());
		http.with(authorizationServer, withDefaults());
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(withDefaults());
		http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));
		// http.exceptionHandling(exceptions -> {
        //     exceptions.defaultAuthenticationEntryPointFor(redirectEntryPoint("/login/oauth2/code/github"), asyncRequestMatcher());
        // });
        http.cors(withDefaults());
		return http.build();
	}

	@Bean
	@Order(SecurityProperties.BASIC_AUTH_ORDER)
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .formLogin(withDefaults())
            .oauth2Login(withDefaults())
            .oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));
		http.exceptionHandling(exceptions -> {
            exceptions.defaultAuthenticationEntryPointFor(unauthorizedEntryPoint(), asyncRequestMatcher());
        });
        http.cors(withDefaults());
		return http.build();
	}

    private static AuthenticationEntryPoint unauthorizedEntryPoint(){
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

	private static RequestMatcher asyncRequestMatcher() {
        var name = "X-Requested-With";
        var value = "XMLHttpRequest";
		return new RequestHeaderRequestMatcher(name, value);
	}

    // private static AuthenticationEntryPoint redirectEntryPoint(String path){
    //     return new LoginUrlAuthenticationEntryPoint(path);
    // }

	// private static RequestMatcher htmlRequestMatcher() {
	// 	MediaTypeRequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
	// 	requestMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
	// 	return requestMatcher;
	// }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * @see UserDetailsServiceAutoConfiguration
     */
    @Bean
	InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties properties,
			ObjectProvider<PasswordEncoder> passwordEncoder) {
		SecurityProperties.User user = properties.getUser();
		List<String> roles = user.getRoles();
		return new InMemoryUserDetailsManager(User.withUsername(user.getName())
			.password(getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
			.roles(StringUtils.toStringArray(roles))
			.build());
	}

    String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
        final String NOOP_PASSWORD_PREFIX = "{noop}";
        final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");
		String password = user.getPassword();
		if (user.isPasswordGenerated()) {
			log.warn(String.format(
					"%n%nUsing generated security password: %s%n%nThis generated password is for development use only. "
							+ "Your security configuration must be updated before running your application in "
							+ "production.%n",
					user.getPassword()));
		}
		if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
			return password;
		}
		return NOOP_PASSWORD_PREFIX + password;
	}

}
