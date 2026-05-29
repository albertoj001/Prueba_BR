package com.empresa.customer.resource;

import com.empresa.customer.dto.CustomerDto;
import com.empresa.customer.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;

@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customers", description = "Operaciones CRUD sobre clientes")
public class CustomerResource {

    private static final Logger LOG = Logger.getLogger(CustomerResource.class);

    @Inject
    CustomerService customerService;

    @POST
    @Operation(
        summary = "Crear un nuevo cliente",
        description = "Crea un cliente con los datos proporcionados. El gentilicio se obtiene " +
                      "automáticamente desde restcountries.com usando el código ISO 3166-1 del país."
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Cliente creado exitosamente",
            content = @Content(schema = @Schema(implementation = CustomerDto.Response.class))),
        @APIResponse(responseCode = "400", description = "Datos de entrada inválidos (validación fallida)"),
        @APIResponse(responseCode = "409", description = "El correo electrónico ya existe"),
        @APIResponse(responseCode = "422", description = "Código de país no válido o no encontrado")
    })
    public Response create(@Valid CustomerDto.CreateRequest request) {
        LOG.debugf("POST /customers - email: %s", request.email);

        CustomerDto.Response created = customerService.create(request);

        URI location = UriBuilder.fromResource(CustomerResource.class)
            .path("{id}")
            .build(created.id);

        return Response.created(location)
            .entity(created)
            .build();
    }

    @GET
    @Operation(
        summary = "Obtener clientes por codigo de pais",
        description = "Retorna todos los clientes. Si se provee el parámetro 'country', " +
                      "filtra por código ISO 3166-1 del país (ej: DO, US, CO)."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Lista de clientes (puede estar vacía)",
            content = @Content(schema = @Schema(implementation = CustomerDto.Response.class)))
    })
    public Response getAll(
        @Parameter(description = "Código ISO 3166-1 para filtrar por país (ej: DO, US)")
        @QueryParam("country") String countryCode
    ) {
        List<CustomerDto.Response> customers;

        if (countryCode != null && !countryCode.isBlank()) {
            customers = customerService.findByCountry(countryCode);
        } else {
            customers = customerService.findAll();
        }

        return Response.ok(customers).build();
    }

    @GET
    @Path("/all")
    @Operation(
            summary = "Obtener todos los clientes",
            description = "Retorna todos los clientes registrados en el sistema"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de clientes obtenida exitosamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CustomerDto.Response.class
                            )
                    )
            )
    })
    public Response getAllCustomers() {

        LOG.info("GET /customers/all");

        List<CustomerDto.Response> customers =
                customerService.findAll();

        return Response.ok(customers).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener un cliente por ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = CustomerDto.Response.class))),
        @APIResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public Response getById(
        @Parameter(description = "UUID del cliente", required = true)
        @PathParam("id") String id
    ) {
        CustomerDto.Response customer = customerService.findById(id);
        return Response.ok(customer).build();
    }

    @PATCH
    @Path("/{id}")
    @Operation(
        summary = "Actualizar un cliente (parcial)",
        description = "Permite actualizar solo: email, dirección, teléfono y país. " +
                      "Los campos no enviados o nulos no se modifican. " +
                      "Si se cambia el país, el gentilicio se actualiza automáticamente."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Cliente actualizado",
            content = @Content(schema = @Schema(implementation = CustomerDto.Response.class))),
        @APIResponse(responseCode = "400", description = "Datos inválidos o sin campos a actualizar"),
        @APIResponse(responseCode = "404", description = "Cliente no encontrado"),
        @APIResponse(responseCode = "409", description = "El correo electrónico ya pertenece a otro cliente"),
        @APIResponse(responseCode = "422", description = "Código de país no válido")
    })
    public Response update(
        @Parameter(description = "UUID del cliente", required = true)
        @PathParam("id") String id,
        @Valid CustomerDto.UpdateRequest request
    ) {
        LOG.debugf("PATCH /customers/%s", id);
        CustomerDto.Response updated = customerService.update(id, request);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar un cliente por ID")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
        @APIResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public Response delete(
        @Parameter(description = "UUID del cliente", required = true)
        @PathParam("id") String id
    ) {
        LOG.debugf("DELETE /customers/%s", id);
        customerService.delete(id);
        return Response.noContent().build();
    }
}
