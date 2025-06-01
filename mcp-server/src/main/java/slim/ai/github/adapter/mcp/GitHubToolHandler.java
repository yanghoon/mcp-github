package slim.ai.github.adapter.mcp;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import slim.ai.annotation.Tools;
import slim.ai.github.adapter.mcp.token.OauthTokenProvider;

@Tools
public class GitHubToolHandler {

    @Autowired
    private OauthTokenProvider tokenProvider;

    /**
     * @see https://github.com/modelcontextprotocol/java-sdk/pull/215
     * @see https://github.com/modelcontextprotocol/java-sdk/pull/215/commits/1f1488677977e0ca6a949d33118350948a8a28af
     */
    @Tool(description = "Get username")
    public String getUsername(ToolContext context) {
        McpSyncServerExchange exchange = (McpSyncServerExchange) context.getContext().get("exchange");
        // McpAsyncServerExchange asyncExchange = exchange.getExchange();
        // McpServerSession session = asyncExchange.getSession();
        // var auth = session.getAuthentication();
        var token = tokenProvider.getAccessToken();
        return "There are no repositories of " + token;
    }
    
    @Tool(description = "Get all repositories of a given user")
    public String getRepoList(
        @ToolParam(description = "GitHub Username") String username
        ) {
        return "There are no repositories of " + username;
    }

}
