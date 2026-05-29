package com.empresa.customer.resource;

import com.empresa.customer.dto.CustomerDto;
import com.empresa.customer.service.CustomerService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import com.empresa.customer.exception.CustomerException;

@QuarkusTest
@DisplayName("CustomerResource")
class CustomerResourceTest {

    @InjectMock
    CustomerService customerService;

    private CustomerDto.Response buildSampleResponse() {
        CustomerDto.Response r = new CustomerDto.Response();
        r.id          = "test-uuid-001";
        r.firstName   = "Juan";
        r.lastName    = "Pérez";
        r.email       = "juan@example.com";
        r.address     = "Calle 1 #100";
        r.phone       = "+18091234567";
        r.countryCode = "DO";
        r.demonym     = "Dominican";
        r.createdAt   = LocalDateTime.now();
        r.updatedAt   = LocalDateTime.now();
        return r;
    }

    @Nested
    @DisplayName("POST /api/v1/customers")
    class PostTests {

        @Test
        @DisplayName("201 Created con Location header al crear cliente válido")
        void shouldReturn201WhenCreated() {
            when(customerService.create(any())).thenReturn(buildSampleResponse());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Juan",
                        "lastName": "Pérez",
                        "email": "juan@example.com",
                        "address": "Calle 1 #100",
                        "phone": "+18091234567",
                        "countryCode": "DO"
                    }
                    """)
            .when()
                .post("/api/v1/customers")
            .then()
                .statusCode(201)
                .header("Location", containsString("/api/v1/customers/"))
                .body("id", equalTo("test-uuid-001"))
                .body("demonym", equalTo("Dominican"));
        }

        @Test
        @DisplayName("400 Bad Request cuando faltan campos requeridos")
        void shouldReturn400WhenMissingRequiredFields() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Juan"
                    }
                    """)
            .when()
                .post("/api/v1/customers")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("400 Bad Request cuando el email tiene formato inválido")
        void shouldReturn400WhenEmailIsInvalid() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Juan",
                        "lastName": "Pérez",
                        "email": "no-es-un-email",
                        "address": "Calle 1",
                        "phone": "+18091234567",
                        "countryCode": "DO"
                    }
                    """)
            .when()
                .post("/api/v1/customers")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("409 Conflict cuando el email ya existe")
        void shouldReturn409WhenEmailDuplicated() {
            when(customerService.create(any()))
                .thenThrow(new CustomerException.DuplicateEmailException("juan@example.com"));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Juan",
                        "lastName": "Pérez",
                        "email": "juan@example.com",
                        "address": "Calle 1",
                        "phone": "+18091234567",
                        "countryCode": "DO"
                    }
                    """)
            .when()
                .post("/api/v1/customers")
            .then()
                .statusCode(409)
                .body("message", containsString("juan@example.com"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers")
    class GetAllTests {

        @Test
        @DisplayName("200 OK con lista de clientes")
        void shouldReturn200WithList() {
            when(customerService.findAll()).thenReturn(List.of(buildSampleResponse()));

            given()
            .when()
                .get("/api/v1/customers")
            .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo("test-uuid-001"));
        }

        @Test
        @DisplayName("200 OK con lista vacía cuando no hay clientes")
        void shouldReturn200WithEmptyList() {
            when(customerService.findAll()).thenReturn(Collections.emptyList());

            given()
            .when()
                .get("/api/v1/customers")
            .then()
                .statusCode(200)
                .body("size()", equalTo(0));
        }

        @Test
        @DisplayName("200 OK filtrando por país con ?country=DO")
        void shouldReturn200FilteredByCountry() {
            when(customerService.findByCountry("DO")).thenReturn(List.of(buildSampleResponse()));

            given()
                .queryParam("country", "DO")
            .when()
                .get("/api/v1/customers")
            .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].countryCode", equalTo("DO"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("200 OK cuando el cliente existe")
        void shouldReturn200WhenFound() {
            when(customerService.findById("test-uuid-001")).thenReturn(buildSampleResponse());

            given()
            .when()
                .get("/api/v1/customers/test-uuid-001")
            .then()
                .statusCode(200)
                .body("id", equalTo("test-uuid-001"))
                .body("firstName", equalTo("Juan"));
        }

        @Test
        @DisplayName("404 Not Found cuando el ID no existe")
        void shouldReturn404WhenNotFound() {
            when(customerService.findById(anyString()))
                .thenThrow(new CustomerException.CustomerNotFoundException("bad-id"));

            given()
            .when()
                .get("/api/v1/customers/bad-id")
            .then()
                .statusCode(404)
                .body("message", containsString("bad-id"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/customers/{id}")
    class PatchTests {

        @Test
        @DisplayName("200 OK al actualizar campos válidos")
        void shouldReturn200WhenUpdated() {
            CustomerDto.Response updated = buildSampleResponse();
            updated.address = "Nueva Dirección 999";
            when(customerService.update(anyString(), any())).thenReturn(updated);

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "address": "Nueva Dirección 999" }
                    """)
            .when()
                .patch("/api/v1/customers/test-uuid-001")
            .then()
                .statusCode(200)
                .body("address", equalTo("Nueva Dirección 999"));
        }

        @Test
        @DisplayName("400 cuando el body está vacío (sin campos)")
        void shouldReturn400WhenBodyIsEmpty() {
            when(customerService.update(anyString(), any()))
                .thenThrow(new CustomerException.EmptyUpdateException());

            given()
                .contentType(ContentType.JSON)
                .body("{}")
            .when()
                .patch("/api/v1/customers/test-uuid-001")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{id}")
    class DeleteTests {

        @Test
        @DisplayName("204 No Content al eliminar cliente existente")
        void shouldReturn204WhenDeleted() {
            given()
            .when()
                .delete("/api/v1/customers/test-uuid-001")
            .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("404 Not Found cuando el cliente a eliminar no existe")
        void shouldReturn404WhenNotFound() {
            doThrow(new CustomerException.CustomerNotFoundException("ghost-id"))
                .when(customerService).delete("ghost-id");

            given()
            .when()
                .delete("/api/v1/customers/ghost-id")
            .then()
                .statusCode(404);
        }
    }
}
