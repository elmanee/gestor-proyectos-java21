package com.mx.cbtis.gestor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @org.springframework.test.context.ActiveProfiles("test")
class SecurityConfigurationTest {
    @Autowired ApplicationContext context;
    @Test void noSeActivanXsltNiFragmentosSse() {
        assertTrue(context.getBeansOfType(ViewResolver.class).values().stream().noneMatch(v -> v.getClass().getName().contains("Xslt")));
        var handlers=context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        handlers.getHandlerMethods().forEach((mapping,handler) -> {
            assertFalse(mapping.getProducesCondition().toString().contains("text/event-stream"));
            assertFalse(handler.getReturnType().getParameterType().getName().contains("SseEmitter"));
            assertFalse(handler.getReturnType().getParameterType().getName().contains("FragmentsRendering"));
        });
    }
}
