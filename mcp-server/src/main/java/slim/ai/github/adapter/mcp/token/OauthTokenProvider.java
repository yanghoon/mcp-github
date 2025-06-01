package slim.ai.github.adapter.mcp.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class OauthTokenProvider {

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    public Object getAccessToken() {
        var securityContext = securityContext();
        var auth = securityContext.getAuthentication();

        if (auth instanceof OAuth2AuthenticationToken token) {
            var registrationId = token.getAuthorizedClientRegistrationId();
            var name = token.getName();
            var client = clientService.loadAuthorizedClient(registrationId, name);
            return client.getAccessToken().getTokenValue();
        };

        return null;
        // throw new IllegalStateException("No OAuth2 Access Token found.");
    }

    private SecurityContext securityContext() {
        return SecurityContextHolder.getContext();
    }
    
}
