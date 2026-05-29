package com.empresa.customer.service;

import com.empresa.customer.client.RestCountriesClient;
import com.empresa.customer.dto.CustomerDto;
import com.empresa.customer.entity.Customer;
import com.empresa.customer.exception.CustomerException;
import com.empresa.customer.mapper.CustomerMapper;
import com.empresa.customer.repository.CustomerRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("CustomerService")
class CustomerServiceTest {

    @Inject
    CustomerService customerService;

    @InjectMock
    CustomerRepository customerRepository;

    @InjectMock
    CustomerMapper customerMapper;

    @InjectMock
    @RestClient
    RestCountriesClient restCountriesClient;

    private CustomerDto.CreateRequest buildCreateRequest() {
        CustomerDto.CreateRequest req = new CustomerDto.CreateRequest();
        req.firstName  = "Juan";
        req.lastName   = "Pérez";
        req.email      = "juan.perez@example.com";
        req.address    = "Calle Primera #10";
        req.phone      = "+18091234567";
        req.countryCode = "DO";
        return req;
    }

    private Customer buildCustomer(String id) {
        Customer c = new Customer();
        c.id           = id;
        c.firstName    = "Juan";
        c.lastName     = "Pérez";
        c.email        = "juan.perez@example.com";
        c.address      = "Calle Primera #10";
        c.phone        = "+18091234567";
        c.countryCode  = "DO";
        c.demonym      = "Dominican";
        c.createdAt    = LocalDateTime.now();
        c.updatedAt    = LocalDateTime.now();
        return c;
    }

    private CustomerDto.Response buildResponse(String id) {
        CustomerDto.Response r = new CustomerDto.Response();
        r.id           = id;
        r.firstName    = "Juan";
        r.lastName     = "Pérez";
        r.email        = "juan.perez@example.com";
        r.address      = "Calle Primera #10";
        r.phone        = "+18091234567";
        r.countryCode  = "DO";
        r.demonym      = "Dominican";
        r.createdAt    = LocalDateTime.now();
        r.updatedAt    = LocalDateTime.now();
        return r;
    }

    private RestCountriesClient.CountryInfo buildCountryInfo(String demonymValue) {
        RestCountriesClient.CountryInfo info    = new RestCountriesClient.CountryInfo();
        info.demonyms                           = new RestCountriesClient.Demonyms();
        info.demonyms.eng                       = new RestCountriesClient.English();
        info.demonyms.eng.m                     = demonymValue;
        return info;
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Debe crear cliente exitosamente cuando los datos son válidos")
        void shouldCreateCustomerSuccessfully() {
            CustomerDto.CreateRequest req = buildCreateRequest();
            Customer entity    = buildCustomer("uuid-001");
            CustomerDto.Response expected = buildResponse("uuid-001");

            when(customerRepository.findByEmail(req.email)).thenReturn(Optional.empty());
            when(restCountriesClient.getCountryByCode(anyString(), anyString()))
                .thenReturn(buildCountryInfo("Dominican"));
            when(customerMapper.toEntity(req)).thenReturn(entity);
            when(customerMapper.toResponse(entity)).thenReturn(expected);
            doNothing().when(customerRepository).persist(any(Customer.class));

            CustomerDto.Response result = customerService.create(req);

            assertNotNull(result);
            assertEquals("uuid-001", result.id);
            assertEquals("Dominican", result.demonym);
            verify(customerRepository).persist(any(Customer.class));
        }

        @Test
        @DisplayName("Debe lanzar DuplicateEmailException cuando el email ya existe")
        void shouldThrowWhenEmailAlreadyExists() {
            CustomerDto.CreateRequest req = buildCreateRequest();
            Customer existing = buildCustomer("existing-id");

            when(customerRepository.findByEmail(req.email)).thenReturn(Optional.of(existing));

            CustomerException.DuplicateEmailException ex = assertThrows(
                CustomerException.DuplicateEmailException.class,
                () -> customerService.create(req)
            );

            assertTrue(ex.getMessage().contains(req.email));
            verify(customerRepository, never()).persist(any(Customer.class));
        }

        @Test
        @DisplayName("Debe lanzar InvalidCountryCodeException cuando el país no existe (404 del servicio externo)")
        void shouldThrowWhenCountryCodeIsInvalid() {
            CustomerDto.CreateRequest req = buildCreateRequest();
            req.countryCode = "ZZZ";

            when(customerRepository.findByEmail(req.email)).thenReturn(Optional.empty());
            // restcountries.com responde 404 para un código inexistente
            when(restCountriesClient.getCountryByCode(anyString(), anyString()))
                .thenThrow(new WebApplicationException(404));

            assertThrows(
                CustomerException.InvalidCountryCodeException.class,
                () -> customerService.create(req)
            );

            verify(customerRepository, never()).persist(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar lista de todos los clientes")
        void shouldReturnAllCustomers() {
            Customer c1 = buildCustomer("id-1");
            Customer c2 = buildCustomer("id-2");
            List<Customer> entities = List.of(c1, c2);

            when(customerRepository.listAll()).thenReturn(entities);
            when(customerMapper.toResponseList(entities))
                .thenReturn(List.of(buildResponse("id-1"), buildResponse("id-2")));

            List<CustomerDto.Response> result = customerService.findAll();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay clientes")
        void shouldReturnEmptyListWhenNoCustomers() {
            when(customerRepository.listAll()).thenReturn(Collections.emptyList());
            when(customerMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            assertTrue(customerService.findAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Debe retornar el cliente cuando el ID existe")
        void shouldReturnCustomerWhenFound() {
            String id = "uuid-001";
            Customer customer = buildCustomer(id);

            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.of(customer));
            when(customerMapper.toResponse(customer)).thenReturn(buildResponse(id));

            CustomerDto.Response result = customerService.findById(id);

            assertNotNull(result);
            assertEquals(id, result.id);
        }

        @Test
        @DisplayName("Debe lanzar CustomerNotFoundException cuando el ID no existe")
        void shouldThrowWhenNotFound() {
            String id = "nonexistent-id";
            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.empty());

            CustomerException.CustomerNotFoundException ex = assertThrows(
                CustomerException.CustomerNotFoundException.class,
                () -> customerService.findById(id)
            );

            assertEquals(id, ex.getCustomerId());
        }
    }

    @Nested
    @DisplayName("findByCountry()")
    class FindByCountryTests {

        @Test
        @DisplayName("Debe retornar clientes filtrados por país")
        void shouldReturnCustomersByCountry() {
            Customer c = buildCustomer("id-1");

            when(customerRepository.findByCountryCode("DO")).thenReturn(List.of(c));
            when(customerMapper.toResponseList(List.of(c))).thenReturn(List.of(buildResponse("id-1")));

            List<CustomerDto.Response> result = customerService.findByCountry("DO");

            assertEquals(1, result.size());
            assertEquals("DO", result.get(0).countryCode);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Debe actualizar solo los campos permitidos")
        void shouldUpdateAllowedFields() {
            String id = "uuid-001";
            Customer existing = buildCustomer(id);
            CustomerDto.Response expected = buildResponse(id);
            expected.address = "Nueva Dirección 200";

            CustomerDto.UpdateRequest req = new CustomerDto.UpdateRequest();
            req.address = "Nueva Dirección 200";

            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));
            when(customerMapper.toResponse(existing)).thenReturn(expected);
            doNothing().when(customerMapper).updateEntity(req, existing);
            doNothing().when(customerRepository).persist(any(Customer.class));

            CustomerDto.Response result = customerService.update(id, req);

            assertNotNull(result);
            verify(customerMapper).updateEntity(req, existing);
        }

        @Test
        @DisplayName("Debe lanzar EmptyUpdateException cuando no se envía ningún campo")
        void shouldThrowWhenRequestIsEmpty() {
            CustomerDto.UpdateRequest req = new CustomerDto.UpdateRequest();

            assertThrows(
                CustomerException.EmptyUpdateException.class,
                () -> customerService.update("any-id", req)
            );

            verify(customerRepository, never()).persist(any(Customer.class));
        }

        @Test
        @DisplayName("Debe lanzar DuplicateEmailException cuando el nuevo email ya existe en otro cliente")
        void shouldThrowWhenNewEmailBelongsToAnotherCustomer() {
            String id = "uuid-001";
            Customer existing = buildCustomer(id);
            String newEmail = "otro@example.com";

            CustomerDto.UpdateRequest req = new CustomerDto.UpdateRequest();
            req.email = newEmail;

            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));
            when(customerRepository.existsByEmailAndIdNot(newEmail, id)).thenReturn(true);

            assertThrows(
                CustomerException.DuplicateEmailException.class,
                () -> customerService.update(id, req)
            );
        }

        @Test
        @DisplayName("Debe actualizar gentilicio cuando se cambia el país")
        void shouldUpdateDemonymWhenCountryChanges() {
            String id = "uuid-001";
            Customer existing = buildCustomer(id);
            existing.countryCode = "DO";

            CustomerDto.UpdateRequest req = new CustomerDto.UpdateRequest();
            req.countryCode = "US";

            CustomerDto.Response expected = buildResponse(id);
            expected.demonym = "American";

            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));
            when(restCountriesClient.getCountryByCode("US", "demonyms"))
                .thenReturn(buildCountryInfo("American"));
            doNothing().when(customerMapper).updateEntity(req, existing);
            when(customerMapper.toResponse(existing)).thenReturn(expected);
            doNothing().when(customerRepository).persist(any(Customer.class));

            CustomerDto.Response result = customerService.update(id, req);

            assertEquals("American", result.demonym);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Debe eliminar el cliente cuando el ID existe")
        void shouldDeleteCustomerSuccessfully() {
            String id = "uuid-001";
            Customer existing = buildCustomer(id);

            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));
            doNothing().when(customerRepository).delete(existing);

            assertDoesNotThrow(() -> customerService.delete(id));
            verify(customerRepository).delete(existing);
        }

        @Test
        @DisplayName("Debe lanzar CustomerNotFoundException cuando el ID no existe")
        void shouldThrowWhenCustomerNotFound() {
            String id = "nonexistent";
            when(customerRepository.findByIdOptional(id)).thenReturn(Optional.empty());

            assertThrows(
                CustomerException.CustomerNotFoundException.class,
                () -> customerService.delete(id)
            );

            verify(customerRepository, never()).delete(any());
        }
    }
}
