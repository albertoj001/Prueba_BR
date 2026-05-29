package com.empresa.customer.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v3.1")
@RegisterRestClient(configKey = "restcountries")
@Produces(MediaType.APPLICATION_JSON)
public interface RestCountriesClient {

    @GET
    @Path("/alpha/{code}")
    CountryInfo getCountryByCode(
            @PathParam("code") String code,
            @QueryParam("fields") String fields
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    class CountryInfo {

        public Demonyms demonyms;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Demonyms {

        public English eng;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class English {

        public String m;
        public String f;
    }
}