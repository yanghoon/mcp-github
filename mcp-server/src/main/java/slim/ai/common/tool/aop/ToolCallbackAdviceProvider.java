package slim.ai.common.tool.aop;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToolCallbackAdviceProvider implements ToolCallbackProvider {

    @NonNull
    private final ToolCallbackProvider delegate;

    @NonNull
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
            return delegate.call(toolInput, null);
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            try {
                return delegate.call(toolInput, toolContext);
            } catch (Exception e) {
                var def = delegate.getToolDefinition();
                var meta = delegate.getToolMetadata();
                return Optional.ofNullable(advice.onError(e, toolInput, toolContext, def, meta))
                            .orElseThrow(() -> new RuntimeException("Tool callback failed", e));
            }
        }

    }

}
