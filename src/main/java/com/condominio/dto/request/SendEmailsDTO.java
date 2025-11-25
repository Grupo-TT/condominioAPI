package com.condominio.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class SendEmailsDTO {
    private List<String> emails;  // lista de correos
    private String subject;       // título del correo
    private String message;       // texto del correo
    private MultipartFile file;   // imagen o documento (opcional)

}
