package com.mx.cbtis.gestor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest
class Ejercicio5MyApiGestorApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
    org.springframework.context.ApplicationContext context;

	@Test
	void contextLoads() {
        org.junit.jupiter.api.Assertions.assertFalse(context.getBeansWithAnnotation(org.springframework.web.bind.annotation.RestController.class).isEmpty() && context.getBeansWithAnnotation(org.springframework.stereotype.Controller.class).isEmpty());
	}

}
