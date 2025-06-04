package slim.ai.github.adapter.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("slim.ai.github.adapter.mcp")
public class FeignClientConfig {
    
}
