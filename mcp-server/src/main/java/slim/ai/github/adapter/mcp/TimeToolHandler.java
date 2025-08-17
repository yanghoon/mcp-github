package slim.ai.github.adapter.mcp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;

import slim.ai.annotation.Tools;

@Tools
public class TimeToolHandler {

    /**
     * @see mcp-server/src/main/java/slim/ai/github/adapter/mcp/GitHubToolHandler.java
     */
    @Tool(description = "Get current time")
    public String get_current_time(boolean locale) {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

}
