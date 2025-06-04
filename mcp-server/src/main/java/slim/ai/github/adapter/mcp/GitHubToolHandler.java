package slim.ai.github.adapter.mcp;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import lombok.RequiredArgsConstructor;
import slim.ai.annotation.Tools;
import slim.ai.github.adapter.mcp.token.GitHubAccessTokenProvider;

@Tools
@RequiredArgsConstructor
public class GitHubToolHandler {

    private final GitHubAccessTokenProvider tokenProvider;
    private final GitHubRestClient restClient;

    /**
     * @see https://github.com/modelcontextprotocol/java-sdk/pull/215
     * @see https://github.com/modelcontextprotocol/java-sdk/pull/215/commits/1f1488677977e0ca6a949d33118350948a8a28af
     */
    @Tool(description = "Get Username")
    public Object getUsername(ToolContext context) {
        return restClient.getUser(headerAuth(context)).toString();
    }
    
    @Tool(description = "Get all repositories of a given user")
    public List<Object> getRepoList(
        @ToolParam(description = "GitHub Username") String username,
        ToolContext context
    ) {
        return restClient.listRepos(username, headerAuth(context));
    }

    private String headerAuth(ToolContext context) {
        var token = tokenProvider.getAccessToken(context);
        return "Bearer " + token;
    }

}
