package org.example;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchReceitaResponse {
    public List<ReceitaRepresentation> receitas;
    public PaginationMetadata pagination;
    public Map<String, String> _links;

    public SearchReceitaResponse() {
    }

    public static SearchReceitaResponse from(List<Receita> receitas, UriInfo uriInfo, String query,
                                             String sort, String direction, int page, int size,
                                             long totalElements, long totalPages) {
        SearchReceitaResponse response = new SearchReceitaResponse();

        // Convert books to representations
        response.receitas = receitas.stream()
                .map(receita -> ReceitaRepresentation.from(receita, uriInfo))
                .toList();

        // Add pagination metadata
        response.pagination = new PaginationMetadata();
        response.pagination.page = page;
        response.pagination.size = size;
        response.pagination.totalElements = totalElements;
        response.pagination.totalPages = totalPages;

        // Add HATEOAS links
        response._links = buildLinks(uriInfo, query, sort, direction, page, size, totalPages);

        return response;
    }

    private static Map<String, String> buildLinks(UriInfo uriInfo, String query, String sort,
                                                  String direction, int page, int size, long totalPages) {
        Map<String, String> links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();
        String baseUrl = baseUri + "receitas/search";

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

        links.put("receitas", baseUri + "receitas");

        return links;
    }

    public static class PaginationMetadata {
        public int page;
        public int size;
        public long totalElements;
        public long totalPages;
    }
}