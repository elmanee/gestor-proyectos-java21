package com.mx.cbtis.bromitas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Ejercicio5ClienteApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
    org.springframework.context.ApplicationContext context;

	@Test
	void contextLoads() {
        org.junit.jupiter.api.Assertions.assertFalse(context.getBeansWithAnnotation(org.springframework.web.bind.annotation.RestController.class).isEmpty() && context.getBeansWithAnnotation(org.springframework.stereotype.Controller.class).isEmpty());
	}

}
