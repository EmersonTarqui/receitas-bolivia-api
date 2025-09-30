package org.example;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class CategoriaRepresentation {

    public Long id;
    public String nome;
    public Map<String, String> _links;

    public CategoriaRepresentation() {
    }

    public static CategoriaRepresentation from(Categoria categoria, UriInfo uriInfo) {
        CategoriaRepresentation rep = new CategoriaRepresentation();
        rep.id = categoria.id;
        rep.nome = categoria.nome;

        rep._links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();

        rep._links.put("self", baseUri + "categorias/" + categoria.id);
        rep._links.put("all", baseUri + "categorias");
        rep._links.put("delete", baseUri + "categorias/" + categoria.id);
        rep._links.put("update", baseUri + "categorias/" + categoria.id);
        rep._links.put("search", baseUri + "categorias/search");

        return rep;
    }
}