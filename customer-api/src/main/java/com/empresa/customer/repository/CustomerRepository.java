package com.empresa.customer.repository;

import com.empresa.customer.entity.Customer;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, String> {

    public List<Customer> findByCountryCode(String countryCode) {
        return list("countryCode", countryCode.toUpperCase());
    }

    public Optional<Customer> findByEmail(String email) {
        return find("LOWER(email) = LOWER(?1)", email).firstResultOptional();
    }

    // Para validar el email en updates sin que el propio cliente cuente como duplicado.
    public boolean existsByEmailAndIdNot(String email, String excludeId) {
        return count("LOWER(email) = LOWER(?1) AND id != ?2", email, excludeId) > 0;
    }
}
