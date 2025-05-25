package slim.ai.github.adapter.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.scheduler.Schedulers;
import slim.ai.annotation.Tools;
import slim.ai.github.adapter.mcp.token.OauthTokenProvider;

@Tools
public class GitHubToolHandler {

    @Autowired
    private OauthTokenProvider tokenProvider;

    @Tool(description = "Get username")
    public String getUsername() {
        var token = tokenProvider.getAccessToken().subscribeOn(Schedulers.boundedElastic()).block();
        return "There are no repositories of " + token;
    }
    
    @Tool(description = "Get all repositories of a given user")
    public String getRepoList(
        @ToolParam(description = "GitHub Username") String username) {
        return "There are no repositories of " + username;
    }

}
