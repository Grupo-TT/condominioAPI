package com.condominio.util.listeners;

import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.service.implementation.EmailService;
import com.condominio.util.events.RepliedSolicitudEvent;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SolicitudReservaListener {

    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(SolicitudReservaListener.class);


    public SolicitudReservaListener(EmailService emailService) {
        this.emailService = emailService;
    }
    @EventListener
    public void handleSolicitudContestada(RepliedSolicitudEvent event) throws MessagingException {
        log.info("SolicitudReservaListener - evento recibido para: {}", event.getEmailPropietario());
        SolicitudReservaRecursoDTO solicitudReservaRecursoDTO = event.getSolicitudReservaRecursoDTO();
        String emailPropietario = event.getEmailPropietario();
        emailService.enviarSolicitud(emailPropietario, solicitudReservaRecursoDTO);
        log.info("SolicitudReservaListener - llamada a EmailService realizada");
    }
}
