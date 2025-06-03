package slim.ai.github.adapter.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import slim.ai.annotation.Tools;
import slim.ai.github.adapter.config.aop.McpServerAdvice;

@Configuration
// @EnableLoadTimeWeaving
public class McpConfig {
        
    @Bean
    ToolCallbackProvider githubTools(ApplicationContext context) {
        var tools = context.getBeansWithAnnotation(Tools.class);
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools.values().toArray())
                .build();
    }

    @Bean
    McpServerAdvice mcpServerAdvice(OAuth2AuthorizedClientService service) {
        return new McpServerAdvice(service);
    }
    
}
