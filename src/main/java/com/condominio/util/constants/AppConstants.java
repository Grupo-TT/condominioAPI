package com.condominio.util.constants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;





public class AppConstants {

    private AppConstants() {

    }

    public static final String PASSWORD_HTML="email/password-temporal";
    public static final String LOGIN_URL="http://localhost:8080";
    public static final String EMAIL_SUBJECT="Tu contraseña temporal - Condominio Flor del Campo";
    public static final String EMAIL_PAGO_SUBJECT="Pago - Condominio Flor del Campo";
    public static final String EMAIL_SOLICITUD_SUBJECT="Solicitud de Reserva de Recurso Comun - Condominio Flor del Campo";
    public static final String PAGO_HTML="email/pago";
    public static final String SOLICITUD_HTML="email/solicitud";
    public static final ZoneId ZONE = ZoneId.of("America/Bogota");
    public static final DateTimeFormatter READABLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

}
