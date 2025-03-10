package com.java_avanade.spring_app.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe que contém os DTOs relacionados à autenticação e registro de usuários
 */
public class AuthDTO {

    /**
     * DTO para requisições de login
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username não pode estar em branco")
        private String username;

        @NotBlank(message = "Senha não pode estar em branco")
        private String password;
    }

    /**
     * DTO para requisições de registro de novos usuários
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Username não pode estar em branco")
        @Size(min = 3, max = 20, message = "Username deve ter entre 3 e 20 caracteres")
        private String username;

        @NotBlank(message = "Senha não pode estar em branco")
        @Size(min = 6, max = 40, message = "Senha deve ter entre 6 e 40 caracteres")
        private String password;

        @NotBlank(message = "Email não pode estar em branco")
        @Email(message = "Email deve ser válido")
        private String email;

        @NotBlank(message = "Nome não pode estar em branco")
        @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
        private String name;

        @NotBlank(message = "Tipo de usuário não pode estar em branco")
        private String userType; // "CLIENT" ou "AFFILIATE"
    }

    /**
     * DTO para resposta com token JWT após autenticação ou registro bem-sucedido
     */
    @Data
    @NoArgsConstructor
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private String userType;

        public JwtResponse(String token, Long id, String username, String email, String userType) {
            this.token = token;
            this.id = id;
            this.username = username;
            this.email = email;
            this.userType = userType;
        }
    }
}