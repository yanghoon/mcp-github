package slim.ai.common.tool.aop;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public interface ToolCallbackAdvice {

    String onError(Exception e, String toolInput, ToolContext toolContext, ToolDefinition def, ToolMetadata meta);

}
