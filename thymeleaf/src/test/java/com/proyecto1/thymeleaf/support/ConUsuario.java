package com.proyecto1.thymeleaf.support;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Simula un usuario autenticado con JWT en las pruebas de controladores:
//   @ConUsuario                              -> ADMIN de la empresa 1
//   @ConUsuario(rol = "LECTURA", empresaId = 2)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithSecurityContext(factory = ConUsuarioFactory.class)
public @interface ConUsuario {

    long empresaId() default 1L;

    long usuarioId() default 1L;

    String rol() default "ADMIN";

    String correo() default "admin@demo.com";
}
