package com.condominio.controller;

import com.condominio.dto.request.SendEmailsDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.implementation.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-many")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<String> sendToMany(
            @ModelAttribute SendEmailsDTO request
    ) {

        emailService.sendToMany(request);


        return new SuccessResult<>(
                "Incertidumbre absoluta, revisar correo.",
                null
        );
    }
}
