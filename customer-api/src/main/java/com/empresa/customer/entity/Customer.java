package com.empresa.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customer_email", columnList = "email", unique = true),
        @Index(name = "idx_customer_country", columnList = "country_code")
    }
)
public class Customer extends PanacheEntityBase {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    public String id;

    @NotBlank(message = "El primer nombre es requerido")
    @Size(min = 1, max = 100, message = "El primer nombre debe tener entre 1 y 100 caracteres")
    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @Size(max = 100, message = "El segundo nombre no puede superar 100 caracteres")
    @Column(name = "middle_name", length = 100)
    public String middleName;

    @NotBlank(message = "El primer apellido es requerido")
    @Size(min = 1, max = 100, message = "El primer apellido debe tener entre 1 y 100 caracteres")
    @Column(name = "last_name", nullable = false, length = 100)
    public String lastName;

    @Size(max = 100, message = "El segundo apellido no puede superar 100 caracteres")
    @Column(name = "second_last_name", length = 100)
    public String secondLastName;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @NotBlank(message = "La dirección es requerida")
    @Size(max = 500, message = "La dirección no puede superar 500 caracteres")
    @Column(name = "address", nullable = false, length = 500)
    public String address;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}$",
        message = "El teléfono debe ser un número válido (formato E.164, ej: +18091234567)"
    )
    @Column(name = "phone", nullable = false, length = 20)
    public String phone;

    @NotBlank(message = "El código de país es requerido")
    @Size(min = 2, max = 3, message = "El código de país debe ser ISO 3166-1 alfa-2 (2 letras) o alfa-3 (3 letras)")
    @Column(name = "country_code", nullable = false, length = 3)
    public String countryCode;

    // Gentilicio obtenido de restcountries.com. Se cachea acá para no pegarle
    // al servicio externo en cada lectura.
    @Column(name = "demonym", length = 100)
    public String demonym;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
