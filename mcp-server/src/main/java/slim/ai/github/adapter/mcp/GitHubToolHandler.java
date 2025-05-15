package slim.ai.github.adapter.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import slim.ai.annotation.Tools;

@Tools
public class GitHubToolHandler {
    
    @Tool(description = "Get all repositories of a given user")
    public String getRepoList(
        @ToolParam(description = "GitHub Username") String username) {
        return "There are no repositories of " + username;
    }

}
