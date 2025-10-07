package com.condominio.util.listeners;

import com.condominio.persistence.model.Persona;
import com.condominio.service.implementation.EmailService;
import com.condominio.util.events.CreatedPersonaEvent;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class PersonaListener {

    private final EmailService emailService;

    public PersonaListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePersonaCreada(CreatedPersonaEvent event) throws MessagingException {
        Persona persona = event.getPersona();
        emailService.enviarPasswordTemporal(persona.getUser().getEmail(), String.valueOf(persona.getNumeroDocumento()));
    }
}

