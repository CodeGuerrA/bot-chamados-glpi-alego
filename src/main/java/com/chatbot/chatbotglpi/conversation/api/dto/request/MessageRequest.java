package com.chatbot.chatbotglpi.conversation.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(
            regexp = "^[0-9]{10,20}(@s\\.whatsapp\\.net)?$",
            message = "Formato de telefone inválido. Use apenas números (10-20 dígitos)"
    )
    @Size(min = 10, max = 50, message = "Telefone deve ter entre 10 e 50 caracteres")
    private String phone;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(min = 1, max = 2000, message = "Mensagem deve ter entre 1 e 2000 caracteres")
    private String message;
}
