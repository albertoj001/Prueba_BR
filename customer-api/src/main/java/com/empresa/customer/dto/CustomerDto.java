package com.empresa.customer.dto;

import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

public class CustomerDto {

    @Schema(name = "CreateCustomerRequest", description = "Datos para crear un nuevo cliente")
    public static class CreateRequest {

        @Schema(description = "Primer nombre del cliente", required = true, example = "Juan")
        @NotBlank(message = "El primer nombre es requerido")
        @Size(min = 1, max = 100)
        public String firstName;

        @Schema(description = "Segundo nombre del cliente (opcional)", example = "Carlos")
        @Size(max = 100)
        public String middleName;

        @Schema(description = "Primer apellido del cliente", required = true, example = "Pérez")
        @NotBlank(message = "El primer apellido es requerido")
        @Size(min = 1, max = 100)
        public String lastName;

        @Schema(description = "Segundo apellido del cliente (opcional)", example = "García")
        @Size(max = 100)
        public String secondLastName;

        @Schema(description = "Correo electrónico único del cliente", required = true, example = "juan.perez@example.com")
        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El formato del correo no es válido")
        public String email;

        @Schema(description = "Dirección del cliente", required = true, example = "Calle Primera #10, Santo Domingo")
        @NotBlank(message = "La dirección es requerida")
        @Size(max = 500)
        public String address;

        @Schema(description = "Teléfono en formato E.164", required = true, example = "+18091234567")
        @NotBlank(message = "El teléfono es requerido")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El teléfono debe ser un número válido")
        public String phone;

        @Schema(description = "Código ISO 3166-1 alfa-2 del país", required = true, example = "DO")
        @NotBlank(message = "El código de país es requerido")
        @Size(min = 2, max = 3, message = "Usar código ISO 3166-1 (2 o 3 letras)")
        public String countryCode;
    }

    // PATCH: solo se permiten estos campos. El resto del cliente es inmutable.
    @Schema(name = "UpdateCustomerRequest", description = "Campos actualizables del cliente")
    public static class UpdateRequest {

        @Schema(description = "Nuevo correo electrónico", example = "nuevo.correo@example.com")
        @Email(message = "El formato del correo no es válido")
        public String email;

        @Schema(description = "Nueva dirección", example = "Av. Winston Churchill #100")
        @Size(max = 500)
        public String address;

        @Schema(description = "Nuevo teléfono en formato E.164", example = "+18097654321")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El teléfono debe ser un número válido")
        public String phone;

        @Schema(description = "Nuevo código de país ISO 3166-1", example = "US")
        @Size(min = 2, max = 3)
        public String countryCode;
    }

    @Schema(name = "CustomerResponse", description = "Datos del cliente retornados por la API")
    public static class Response {

        @Schema(description = "Identificador único (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        public String id;

        @Schema(description = "Primer nombre", example = "Juan")
        public String firstName;

        @Schema(description = "Segundo nombre", example = "Carlos")
        public String middleName;

        @Schema(description = "Primer apellido", example = "Pérez")
        public String lastName;

        @Schema(description = "Segundo apellido", example = "García")
        public String secondLastName;

        @Schema(description = "Correo electrónico", example = "juan.perez@example.com")
        public String email;

        @Schema(description = "Dirección", example = "Calle Primera #10, Santo Domingo")
        public String address;

        @Schema(description = "Teléfono", example = "+18091234567")
        public String phone;

        @Schema(description = "Código de país ISO 3166-1", example = "DO")
        public String countryCode;

        @Schema(description = "Gentilicio del país (de restcountries.com)", example = "Dominican")
        public String demonym;

        @Schema(description = "Fecha de creación")
        public LocalDateTime createdAt;

        @Schema(description = "Fecha de última actualización")
        public LocalDateTime updatedAt;
    }
}
