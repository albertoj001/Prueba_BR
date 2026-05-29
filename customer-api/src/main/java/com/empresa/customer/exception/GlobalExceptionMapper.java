package com.empresa.customer.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones de dominio (y las de validación) a respuestas JSON
 * con un formato uniforme: timestamp, status, error, message.
 */
@Provider
public class GlobalExceptionMapper {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Provider
    public static class NotFoundMapper
            implements ExceptionMapper<CustomerException.CustomerNotFoundException> {

        @Override
        public Response toResponse(CustomerException.CustomerNotFoundException ex) {
            LOG.debugf("Cliente no encontrado: %s", ex.getCustomerId());
            return buildError(Response.Status.NOT_FOUND, ex.getMessage());
        }
    }

    @Provider
    public static class DuplicateEmailMapper
            implements ExceptionMapper<CustomerException.DuplicateEmailException> {

        @Override
        public Response toResponse(CustomerException.DuplicateEmailException ex) {
            LOG.infof("Intento de duplicar email: %s", ex.getEmail());
            return buildError(Response.Status.CONFLICT, ex.getMessage());
        }
    }

    @Provider
    public static class InvalidCountryMapper
            implements ExceptionMapper<CustomerException.InvalidCountryCodeException> {

        @Override
        public Response toResponse(CustomerException.InvalidCountryCodeException ex) {
            LOG.warnf("Código de país inválido: %s - %s", ex.getCountryCode(), ex.getMessage());
            return buildError(422, ex.getMessage());
        }
    }

    @Provider
    public static class EmptyUpdateMapper
            implements ExceptionMapper<CustomerException.EmptyUpdateException> {

        @Override
        public Response toResponse(CustomerException.EmptyUpdateException ex) {
            return buildError(Response.Status.BAD_REQUEST, ex.getMessage());
        }
    }

    // Cubre @NotBlank, @Email, @Size, @Pattern de Bean Validation.
    @Provider
    public static class ConstraintViolationMapper
            implements ExceptionMapper<ConstraintViolationException> {

        @Override
        public Response toResponse(ConstraintViolationException ex) {
            Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    this::extractFieldName,
                    ConstraintViolation::getMessage,
                    (v1, v2) -> v1
                ));

            Map<String, Object> body = baseError(Response.Status.BAD_REQUEST, "Errores de validación");
            body.put("violations", violations);

            return Response.status(Response.Status.BAD_REQUEST)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
        }

        // El path viene como "metodo.parametro.campo"; nos quedamos con el campo.
        private String extractFieldName(ConstraintViolation<?> violation) {
            String path = violation.getPropertyPath().toString();
            int lastDot = path.lastIndexOf('.');
            return lastDot >= 0 ? path.substring(lastDot + 1) : path;
        }
    }

    static Response buildError(Response.Status status, String message) {
        return Response.status(status)
            .entity(baseError(status, message))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    static Response buildError(int statusCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", statusCode);
        body.put("message", message);
        return Response.status(statusCode)
            .entity(body)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    static Map<String, Object> baseError(Response.Status status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.getStatusCode());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
