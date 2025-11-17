package org.example;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchIngredienteResponse {
    public List<IngredienteRepresentation> ingredientes;
    public PaginationMetadata pagination;
    public Map<String, String> _links;

    public SearchIngredienteResponse() {
    }

    public static SearchIngredienteResponse from(List<Ingrediente> ingredientes, UriInfo uriInfo, String query,
                                                 String sort, String direction, int page, int size,
                                                 long totalElements, long totalPages) {
        SearchIngredienteResponse response = new SearchIngredienteResponse();

        response.ingredientes = ingredientes.stream()
                .map(ingrediente -> IngredienteRepresentation.from(ingrediente, uriInfo))
                .toList();

        response.pagination = new PaginationMetadata(page, size, totalElements, totalPages);
        response._links = buildLinks(uriInfo, query, sort, direction, page, size, totalPages);

        return response;
    }

    private static Map<String, String> buildLinks(UriInfo uriInfo, String query, String sort,
                                                  String direction, int page, int size, long totalPages) {
        Map<String, String> links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();
        String baseUrl = baseUri + "ingredientes/search";

        StringBuilder params = new StringBuilder();
        if (query != null && !query.isBlank()) {
            params.append("q=").append(query).append("&");
        }
        params.append("sort=").append(sort)
                .append("&direction=").append(direction)
                .append("&size=").append(size);

        links.put("self", baseUrl + "?" + params + "&page=" + page);
        links.put("first", baseUrl + "?" + params + "&page=1");
        links.put("last", baseUrl + "?" + params + "&page=" + totalPages);

        if (page > 1) {
            links.put("prev", baseUrl + "?" + params + "&page=" + (page - 1));
        }
        if (page < totalPages) {
            links.put("next", baseUrl + "?" + params + "&page=" + (page + 1));
        }

        links.put("ingredientes", baseUri + "ingredientes");

        return links;
    }

    public static class PaginationMetadata {
        public int page;
        public int size;
        public long totalElements;
        public long totalPages;

        public PaginationMetadata(int page, int size, long totalElements, long totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }
}