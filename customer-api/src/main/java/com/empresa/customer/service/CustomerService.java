package com.empresa.customer.service;

import com.empresa.customer.client.RestCountriesClient;
import com.empresa.customer.dto.CustomerDto;
import com.empresa.customer.entity.Customer;
import com.empresa.customer.exception.CustomerException;
import com.empresa.customer.mapper.CustomerMapper;
import com.empresa.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class CustomerService {

    private static final Logger LOG = Logger.getLogger(CustomerService.class);

    @Inject
    CustomerRepository customerRepository;

    @Inject
    CustomerMapper customerMapper;

    @Inject
    @RestClient
    RestCountriesClient restCountriesClient;

    @Transactional
    public CustomerDto.Response create(CustomerDto.CreateRequest request) {

        customerRepository.findByEmail(request.email)
                .ifPresent(existing -> {
                    throw new CustomerException.DuplicateEmailException(request.email);
                });

        String normalizedCountryCode = request.countryCode.toUpperCase();

        String demonym = fetchDemonym(normalizedCountryCode);

        Customer customer = customerMapper.toEntity(request);

        customer.countryCode = normalizedCountryCode;
        customer.demonym = demonym;

        customerRepository.persist(customer);

        LOG.infof("Cliente creado con ID: %s", customer.id);

        return customerMapper.toResponse(customer);
    }

    public List<CustomerDto.Response> findAll() {
        return customerMapper.toResponseList(customerRepository.listAll());
    }

    public List<CustomerDto.Response> findByCountry(String countryCode) {

        String normalizedCountryCode = countryCode.toUpperCase();

        return customerMapper.toResponseList(
                customerRepository.findByCountryCode(normalizedCountryCode)
        );
    }

    public CustomerDto.Response findById(String id) {

        Customer customer = customerRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new CustomerException.CustomerNotFoundException(id)
                );

        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerDto.Response update(
            String id,
            CustomerDto.UpdateRequest request
    ) {

        if (request.email == null
                && request.address == null
                && request.phone == null
                && request.countryCode == null) {

            throw new CustomerException.EmptyUpdateException();
        }

        Customer customer = customerRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new CustomerException.CustomerNotFoundException(id)
                );

        // Validar email duplicado
        if (request.email != null
                && !request.email.equalsIgnoreCase(customer.email)) {

            if (customerRepository.existsByEmailAndIdNot(
                    request.email,
                    id
            )) {

                throw new CustomerException.DuplicateEmailException(
                        request.email
                );
            }
        }

        // Actualizar país + gentilicio
        if (request.countryCode != null
                && !request.countryCode.equalsIgnoreCase(customer.countryCode)) {

            String normalizedCountryCode =
                    request.countryCode.toUpperCase();

            customer.countryCode = normalizedCountryCode;

            customer.demonym =
                    fetchDemonym(normalizedCountryCode);
        }

        customerMapper.updateEntity(request, customer);

        customerRepository.persist(customer);

        LOG.infof("Cliente actualizado con ID: %s", id);

        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void delete(String id) {

        Customer customer = customerRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new CustomerException.CustomerNotFoundException(id)
                );

        customerRepository.delete(customer);

        LOG.infof("Cliente eliminado con ID: %s", id);
    }

    /**
     * Obtiene el gentilicio desde RestCountries.
     */
    private String fetchDemonym(String countryCode) {

        try {

            RestCountriesClient.CountryInfo country =
                    restCountriesClient.getCountryByCode(
                            countryCode,
                            "demonyms"
                    );

            LOG.infof("Respuesta RestCountries: %s", country);

            if (country == null
                    || country.demonyms == null
                    || country.demonyms.eng == null) {

                LOG.warnf(
                        "No se encontró gentilicio para %s",
                        countryCode
                );

                return countryCode;
            }

            String demonym =
                    country.demonyms.eng.m != null
                            ? country.demonyms.eng.m
                            : country.demonyms.eng.f;

            if (demonym == null || demonym.isBlank()) {

                LOG.warnf(
                        "Gentilicio vacío para %s",
                        countryCode
                );

                return countryCode;
            }

            return demonym;

        } catch (WebApplicationException e) {

            LOG.errorf(
                    "Error consultando RestCountries para %s: %s",
                    countryCode,
                    e.getMessage()
            );

            if (e.getResponse() != null
                    && e.getResponse().getStatus() == 404) {

                throw new CustomerException.InvalidCountryCodeException(
                        countryCode
                );
            }

            throw new CustomerException.InvalidCountryCodeException(
                    countryCode,
                    e
            );

        } catch (Exception e) {

            LOG.error("ERROR GENERAL RESTCOUNTRIES", e);

            throw new CustomerException.InvalidCountryCodeException(
                    countryCode,
                    e
            );
        }
    }
}