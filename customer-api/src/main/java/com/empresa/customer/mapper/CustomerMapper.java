package com.empresa.customer.mapper;

import com.empresa.customer.dto.CustomerDto;
import com.empresa.customer.entity.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "demonym", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "countryCode", expression = "java(request.countryCode != null ? request.countryCode.toUpperCase() : null)")
    Customer toEntity(CustomerDto.CreateRequest request);

    CustomerDto.Response toResponse(Customer customer);

    List<CustomerDto.Response> toResponseList(List<Customer> customers);

    // IGNORE en nullValuePropertyMappingStrategy = solo se sobreescriben los campos
    // que vienen en el request (semántica PATCH).
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "middleName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "secondLastName", ignore = true)
    @Mapping(target = "demonym", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "countryCode", expression = "java(request.countryCode != null ? request.countryCode.toUpperCase() : customer.countryCode)")
    void updateEntity(CustomerDto.UpdateRequest request, @MappingTarget Customer customer);
}
