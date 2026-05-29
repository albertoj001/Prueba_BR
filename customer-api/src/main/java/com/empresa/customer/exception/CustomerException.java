package com.empresa.customer.exception;

// Excepciones de dominio. El servicio no sabe de HTTP; el mapper las traduce
// a códigos de estado en la capa REST.
public class CustomerException {

    // -> 404
    public static class CustomerNotFoundException extends RuntimeException {
        private final String customerId;

        public CustomerNotFoundException(String customerId) {
            super(String.format("Cliente no encontrado con ID: %s", customerId));
            this.customerId = customerId;
        }

        public String getCustomerId() {
            return customerId;
        }
    }

    // -> 409
    public static class DuplicateEmailException extends RuntimeException {
        private final String email;

        public DuplicateEmailException(String email) {
            super(String.format("Ya existe un cliente con el correo: %s", email));
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }

    // -> 422
    public static class InvalidCountryCodeException extends RuntimeException {
        private final String countryCode;

        public InvalidCountryCodeException(String countryCode) {
            super(String.format("Código de país inválido o no encontrado: %s", countryCode));
            this.countryCode = countryCode;
        }

        public InvalidCountryCodeException(String countryCode, Throwable cause) {
            super(String.format("No se pudo obtener información del país: %s", countryCode), cause);
            this.countryCode = countryCode;
        }

        public String getCountryCode() {
            return countryCode;
        }
    }

    // -> 400
    public static class EmptyUpdateException extends RuntimeException {
        public EmptyUpdateException() {
            super("La solicitud de actualización debe contener al menos un campo modificable");
        }
    }
}
