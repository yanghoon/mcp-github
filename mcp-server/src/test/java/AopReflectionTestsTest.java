import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @see org.springframework.ai.tool.method.MethodToolCallbackProvider#getToolCallbacks
 */
@SpringBootTest
@SpringJUnitConfig(classes = {AopReflectionTestsTest.AopConfig.class, AopReflectionTestsTest.ToolAspect.class})
@Slf4j
public class AopReflectionTestsTest {

    @Autowired
    private ToolHandler toolHandler;

    @Test
    void test_find_tool_methods() throws Exception {
        var toolObject = toolHandler;
        var toolObjectClass = toolObject.getClass();
        var targetClass = AopUtils.getTargetClass(toolObject);
        
        assertTrue(AopUtils.isAopProxy(toolObject));
        assertNotEquals(ToolHandler.class, toolObjectClass);
        assertEquals(ToolHandler.class, targetClass);

        log.info("toolObject.getClass(): {}", toolObject.getClass());
        log.info("AopUtils.getTargetClass(toolObject): {}", AopUtils.getTargetClass(toolObject));

        var methodFromTargetClass = Stream.of(ReflectionUtils.getDeclaredMethods(targetClass)).filter(m -> m.getName().equals("annotatedMethod")).findFirst().get();
        var methodFromObjectClass = Stream.of(ReflectionUtils.getDeclaredMethods(toolObjectClass)).filter(m -> m.getName().equals("annotatedMethod")).findFirst().orElse(null);

        assertNotNull(methodFromTargetClass);
        assertNotNull(methodFromObjectClass);

        var args = new Object[] { null, null };
        methodFromTargetClass.invoke(toolObject, args);
        methodFromObjectClass.invoke(toolObject, args);
    }

    @Test
    public void testMethodInvokeWithAop() throws Exception {
        var args = new Object[]{ null };
        var toolObject = toolHandler;
        var targetClass = AopUtils.getTargetClass(toolObject);
        Stream.of(ReflectionUtils.getDeclaredMethods(targetClass))
            .forEach(method -> {
                Object result;
                try {
                    result = method.invoke(toolObject, args);
                    System.out.println(result);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    public static class AopConfig {
        @Bean
        ToolHandler toolHandler() { return new ToolHandler(); }
    }

    // Target class for testing
    public static class ToolHandler {
        @Tool
        public String annotatedMethod(String input, Boolean flag) { return "Processed: " + input; }
        // public String sayHello(String name) { return "Hello, " + name; }
    }

    // Aspect for handling @Tool annotation
    @Aspect
    @Component
    public static class ToolAspect {

        @Pointcut("@annotation(org.springframework.ai.tool.annotation.Tool)")
        public void toolAnnotatedMethods() {}

        @Around("toolAnnotatedMethods()")
        public Object aroundToolMethods(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("@Around(before) - {}", joinPoint.getTarget().getClass());

            var result = joinPoint.proceed();

            log.info("@Around(after) - {}", joinPoint.getTarget().getClass());
            return result;
        }

        @After("toolAnnotatedMethods()")
        public void afterToolMethods() {
            log.info("@After");
        }
    }

}