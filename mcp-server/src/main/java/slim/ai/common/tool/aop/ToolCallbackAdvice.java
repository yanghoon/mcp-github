package slim.ai.common.tool.aop;

public interface ToolCallbackAdvice {

    String onError(Exception e) throws RuntimeException;

}
