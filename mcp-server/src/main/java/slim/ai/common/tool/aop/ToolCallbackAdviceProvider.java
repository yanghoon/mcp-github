package slim.ai.common.tool.aop;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToolCallbackAdviceProvider implements ToolCallbackProvider {

    private final ToolCallbackProvider delegate;
    private final ToolCallbackAdvice advice;

    @Override
    public ToolCallback[] getToolCallbacks() {
        return Stream.of(delegate.getToolCallbacks())
            .map(callback -> new AdvicedToolCallback(callback, advice))
            .toArray(ToolCallback[]::new);
    }

    @RequiredArgsConstructor
    private static class AdvicedToolCallback implements ToolCallback {

        private final ToolCallback delegate;
        private final ToolCallbackAdvice advice;

        @Override
        public ToolDefinition getToolDefinition() {
            return delegate.getToolDefinition();
        }

        @Override
        public String call(String toolInput) {
            try {
                return delegate.call(toolInput);
            } catch (Exception e) {
                return Optional.ofNullable(advice.onError(e)).orElseThrow(() -> new RuntimeException("Tool callback failed", e));
            }
        }

    }

}
