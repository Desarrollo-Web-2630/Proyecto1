package com.proyecto1.thymeleaf.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final int LONGITUD_SAL = 16;
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_HASH = 256;

    private PasswordUtil() {
    }

    public static String guardarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        try {
            byte[] sal = generarSal();
            byte[] hash = generarHash(password.trim(), sal);
            return Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo guardar la contraseña", e);
        }
    }

    public static boolean validarPassword(String passwordIngresada, String passwordGuardada) {
        if (passwordIngresada == null || passwordGuardada == null || passwordGuardada.isBlank()) {
            return false;
        }

        try {
            String[] partes = passwordGuardada.split(":");
            if (partes.length != 2) {
                return false;
            }

            byte[] sal = Base64.getDecoder().decode(partes[0]);
            byte[] hashGuardado = Base64.getDecoder().decode(partes[1]);
            byte[] hashCalculado = generarHash(passwordIngresada.trim(), sal);

            return java.util.Arrays.equals(hashGuardado, hashCalculado);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean cumpleReglasBasicas(String password) {
        if (password == null) {
            return false;
        }

        String valor = password.trim();
        if (valor.length() < 8) {
            return false;
        }

        boolean tieneMayuscula = valor.matches(".*[A-Z].*");
        boolean tieneMinuscula = valor.matches(".*[a-z].*");
        boolean tieneNumero = valor.matches(".*\\d.*");

        return tieneMayuscula && tieneMinuscula && tieneNumero;
    }

    private static byte[] generarSal() {
        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);
        return sal;
    }

    private static byte[] generarHash(String password, byte[] sal) throws Exception {
        PBEKeySpec especificacion = new PBEKeySpec(password.toCharArray(), sal, ITERACIONES, LONGITUD_HASH);
        SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return fabrica.generateSecret(especificacion).getEncoded();
    }
}
