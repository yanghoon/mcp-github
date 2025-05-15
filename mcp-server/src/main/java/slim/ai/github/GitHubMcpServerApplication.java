package slim.ai.github;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import slim.ai.github.adapter.mcp.GitHubToolHandler;

@SpringBootApplication
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

}
