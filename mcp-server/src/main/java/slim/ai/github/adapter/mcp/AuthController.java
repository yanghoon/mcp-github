package slim.ai.github.adapter.mcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import slim.ai.github.adapter.mcp.token.OauthTokenProvider;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private OauthTokenProvider tokenProvider;

    @Autowired
    private GitHubToolHandler toolHandler;
    
    @GetMapping("home")
    public String home() {
        return "Welcome to the GitHub MCP Server!";
    }

    @GetMapping("token")
    public Object token() {
        return tokenProvider.getAccessToken();
    }

}
