package mx.gob.morelos.ssi.utils;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null
                && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static String getValidationMessage() {
        return "La contraseña debe tener entre "
                + MIN_LENGTH + " y " + MAX_LENGTH
                + " caracteres e incluir letras y números.";
    }

    public static int getMinLength() { return MIN_LENGTH; }
    public static int getMaxLength() { return MAX_LENGTH; }
}