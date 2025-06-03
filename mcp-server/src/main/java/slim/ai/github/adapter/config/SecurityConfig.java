package slim.ai.github.adapter.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SecurityConfig {

    private static Supplier<AuthenticationEntryPoint> unauthorizedEntryPoint = () -> new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
	private static Supplier<RequestMatcher> asyncRequestMatcher = () -> new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest");

    /**
     * @see org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerWebSecurityConfiguration
     */
    @Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var authorizationServer = OAuth2AuthorizationServerConfigurer.authorizationServer();
		http.securityMatcher(authorizationServer.getEndpointsMatcher());
		http.with(authorizationServer, withDefaults());
		// http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(withDefaults());

        http.cors(withDefaults());

		http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
		    .oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));

		http.exceptionHandling(ex -> {
			var htmlRequestMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
			htmlRequestMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
			ex.defaultAuthenticationEntryPointFor(
				new LoginUrlAuthenticationEntryPoint("/login"),
				htmlRequestMatcher
            );
        });


		return http.build();
	}

	@Bean
	@Order(SecurityProperties.BASIC_AUTH_ORDER)
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults());

		http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
            .formLogin(withDefaults())
            .oauth2Login(withDefaults())
            .oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));

		http.exceptionHandling(ex -> {
            ex.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest")
            );
        });

		return http.build();
	}

    /**
     * @see https://docs.spring.io/spring-authorization-server/reference/guides/how-to-pkce.html#enable-cors
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("*");
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        var source = new UrlBasedCorsConfigurationSource();
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
