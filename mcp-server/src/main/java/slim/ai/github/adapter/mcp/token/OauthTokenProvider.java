package slim.ai.github.adapter.mcp.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class OauthTokenProvider {

    @Autowired
    private ReactiveOAuth2AuthorizedClientService clientService;

    public Mono<Object> getAccessToken() {
        var securityContext = securityContext();
        var auth = securityContext.map(SecurityContext::getAuthentication);

        return auth.flatMap(a-> {
            if (a instanceof OAuth2AuthenticationToken token) {
                var registrationId = token.getAuthorizedClientRegistrationId();
                var name = token.getName();
                var client = clientService.loadAuthorizedClient(registrationId, name);
                return client.map(c -> c.getAccessToken().getTokenValue());
            };
            return Mono.just("Empty Token");
        });
        // throw new IllegalStateException("No OAuth2 Access Token found.");
    }

    private Mono<SecurityContext> securityContext() {
        return ReactiveSecurityContextHolder.getContext();
            // .blockOptional()
            // .get();
        // return SecurityContextHolder.getContext();
    }
    
}
