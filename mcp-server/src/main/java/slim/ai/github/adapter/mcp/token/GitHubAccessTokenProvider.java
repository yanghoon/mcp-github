package slim.ai.github.adapter.mcp.token;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Value;

public class GitHubAccessTokenProvider {

    @Value("${mcpServers.github.env.GITHUB_USERNAME}")
    private String username;

    @Value("${mcpServers.github.env.GITHUB_TOKEN}")
    private String accessToken;

    public String getAccessToken(ToolContext context) {
        return accessToken;
    }
    
}
