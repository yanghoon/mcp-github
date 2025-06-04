package slim.ai.github.adapter.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import slim.ai.annotation.Tools;
import slim.ai.github.adapter.mcp.token.GitHubAccessTokenProvider;
import slim.ai.github.adapter.mcp.token.GitHubAccessTokenProvider.EnvGitHubAccessTokenProvider;

@Configuration
public class McpConfig {
        
    @Bean
    ToolCallbackProvider githubTools(ApplicationContext context) {
        var tools = context.getBeansWithAnnotation(Tools.class);
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools.values().toArray())
                .build();
    }

    @Bean
    GitHubAccessTokenProvider accessTokenProvider() {
        return new EnvGitHubAccessTokenProvider();
    }

}
