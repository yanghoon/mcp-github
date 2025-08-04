package slim.ai.github.adapter.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import slim.ai.annotation.Tools;
import slim.ai.common.tool.aop.ToolCallbackAdvice;
import slim.ai.common.tool.aop.ToolCallbackAdviceProvider;
import slim.ai.github.adapter.mcp.token.GitHubAccessTokenProvider;
import slim.ai.github.adapter.mcp.token.GitHubAccessTokenProvider.EnvGitHubAccessTokenProvider;

@Configuration
public class McpConfig {

    @Bean
    ToolCallbackProvider tools(ApplicationContext context, @Nullable ToolCallbackAdvice advice) {
        var tools = context.getBeansWithAnnotation(Tools.class);
        var provider = MethodToolCallbackProvider.builder()
                .toolObjects(tools.values().toArray())
                .build();
        return new ToolCallbackAdviceProvider(provider, advice);
    }

    @Bean
    GitHubAccessTokenProvider accessTokenProvider() {
        return new EnvGitHubAccessTokenProvider();
    }

}
