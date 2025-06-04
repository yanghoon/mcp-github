package slim.ai.github.adapter.config.aop;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.ReflectionUtils;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
public class McpServerAdvice {

    @Pointcut("execution(* io.modelcontextprotocol.spec.McpServerTransportProvider.setSessionFactory(..))")
    public void setSessionFactory() {};

    @Around("setSessionFactory()")
    public Object setSessionFactory(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();

        if (args.length == 1 && args[0] instanceof McpServerSession.Factory delegate) {
            args[0] = new SecurityContextMcpSessionFactory(delegate);
        }

        return joinPoint.proceed(args);
    }

    private final OAuth2AuthorizedClientService oauthClientService;
    
    @PostConstruct
    public void init() {
        //TODO: Refactor
        McpSecurityContextHolder.oauthClientService = this.oauthClientService;
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class SecurityContextMcpSessionFactory implements McpServerSession.Factory {

        private final McpServerSession.Factory delegate;

        public McpServerSession create(McpServerTransport sessionTransport) {
            var session = delegate.create(sessionTransport);

            var sessionId = session.getId();
            var securityContext = SecurityContextHolder.getContext();
            McpSecurityContextHolder.save(sessionId, securityContext);

            return session;
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    public static class McpSecurityContextHolder {

        private static final Field FIELD_ASYNC_EXCHANGE = ReflectionUtils.findField(McpSyncServerExchange.class, "exchange");
        private static final Field FIELD_MCP_SESSION = ReflectionUtils.findField(McpAsyncServerExchange.class, "session");

        private static Map<String, SecurityContext> contexts = new HashMap<>();

        private static OAuth2AuthorizedClientService oauthClientService;

        static {
            FIELD_ASYNC_EXCHANGE.setAccessible(true);
            FIELD_MCP_SESSION.setAccessible(true);
        }

        public static void save(String sessionId, SecurityContext securityContext) {
            log.debug("Start SecurityContext ... [security-context={}, transport={}]", securityContext);

            contexts.put(sessionId, securityContext);

            log.debug("Save SecurityContext ... [key={}, security-context={}, transport={}]", sessionId, securityContext);
        }

        public static SecurityContext get(String key) {
            return contexts.get(key);
        }

        public static String accessToken(ToolContext context) {
            var syncExchange = context.getContext().get("exchange");
            var asyncExchange = ReflectionUtils.getField(FIELD_ASYNC_EXCHANGE, syncExchange);
            var mcpSession = (McpServerSession) ReflectionUtils.getField(FIELD_MCP_SESSION, asyncExchange);
            var sessionId = mcpSession.getId();

            var securityContext = contexts.get(sessionId);
            var auth = securityContext.getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwt) {
                // var clientId = jwt.getToken().getAudience().get(0);
                var clientId = "github";
                var username = jwt.getName();
                var client = oauthClientService.loadAuthorizedClient(clientId, username);
                return client.getAccessToken().getTokenValue();
            }

            return null;
        }

    }

}
