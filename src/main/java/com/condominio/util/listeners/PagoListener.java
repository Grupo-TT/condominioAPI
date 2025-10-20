package com.condominio.util.listeners;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.service.implementation.EmailService;
import com.condominio.util.events.CreatedPagoEvent;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PagoListener {
    private final EmailService emailService;

    public PagoListener(EmailService emailService) {
        this.emailService = emailService;
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePagoCreado(CreatedPagoEvent event) throws MessagingException {
        ObligacionDTO obligacionDTO = event.getObligacion();
        String emailPropietario = event.getEmailPropietario();
        emailService.enviarPago(emailPropietario, obligacionDTO);
    }
}
