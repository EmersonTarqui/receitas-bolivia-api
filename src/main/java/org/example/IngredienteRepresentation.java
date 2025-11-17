package org.example;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class IngredienteRepresentation {

    public Long id;
    public String nome;
    public Map<String, String> _links;

    public IngredienteRepresentation() {
    }

    public static IngredienteRepresentation from(Ingrediente ingrediente, UriInfo uriInfo) {
        IngredienteRepresentation rep = new IngredienteRepresentation();
        rep.id = ingrediente.id;
        rep.nome = ingrediente.nome;

        rep._links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();

        rep._links.put("self", baseUri + "ingredientes/" + ingrediente.id);
        rep._links.put("all", baseUri + "ingredientes");
        rep._links.put("delete", baseUri + "ingredientes/" + ingrediente.id);
        rep._links.put("update", baseUri + "ingredientes/" + ingrediente.id);
        rep._links.put("search", baseUri + "ingredientes/search");

        return rep;
    }
}