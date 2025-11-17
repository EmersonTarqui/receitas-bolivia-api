package org.example;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.ApiKey.RequiresApiKey;
import org.example.Idempotent;
import io.smallrye.faulttolerance.api.RateLimit;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/v1/ingredientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ingredientes", description = "Operações relacionadas a ingredientes")
public class IngredienteResource {

    @Context
    UriInfo uriInfo;

    private IngredienteRepresentation rep(Ingrediente i) {
        return IngredienteRepresentation.from(i, uriInfo);
    }

    private List<IngredienteRepresentation> repList(List<Ingrediente> ingredientes) {
        return ingredientes.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(
            summary = "getAll (Retorna todos os ingredientes)",
            description = "Retorna uma lista de ingredientes por padrão no formato JSON"
    )
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Ingrediente.class, type = SchemaType.ARRAY)
            )
    )
    @RateLimit(value=15, window=60, windowUnit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod="fallbackParaListarIngredientes")
    public Response getAll() {
        return Response.ok(repList(Ingrediente.listAll())).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "getById (Busca um ingrediente por ID)")
    @Retry(maxRetries = 3)
    @Fallback(fallbackMethod = "fallbackParaIngredientePorId")
    public Response getById(
            @Parameter(description = "Id do ingrediente a ser pesquisado", required = true)
            @PathParam("id") long id) {
        Ingrediente entity = Ingrediente.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search")
    @RateLimit(value = 20, window = 1, windowUnit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fallbackParaBuscaDeIngredientes")
    public Response search(
            @Parameter(description = "Query de busca por nome do ingrediente")
            @QueryParam("q") String q,
            @Parameter(description = "Campo de ordenação da lista de retorno")
            @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação ascendente/descendente")
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        Set<String> allowed = Set.of("id", "nome");
        if (!allowed.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Ingrediente> query = (q == null || q.isBlank())
                ? Ingrediente.findAll(sortObj)
                : Ingrediente.find("lower(nome) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Ingrediente> ingredientes = query.page(effectivePage, size).list();

        SearchIngredienteResponse response = SearchIngredienteResponse.from(
                ingredientes, uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );

        return Response.ok(response).build();
    }

    @POST
    @RequiresApiKey
    @Idempotent
    @Transactional
    @Operation(summary = "Insert (Cria um novo ingrediente)")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Ingrediente.class,
                            example = "{\"nome\": \"Farinha de Trigo\"}"
                    )
            )
    )
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Ingrediente.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class))
    )
    @Parameter(
            name = "X-Idempotency-Key",
            description = "Chave de idempotência (ex: UUIDv7)",
            in = ParameterIn.HEADER,
            schema = @Schema(type = SchemaType.STRING)
    )
    @Parameter(
            name = "X-API-Key",
            in = ParameterIn.HEADER,
            description = "Chave da API para autenticação"
    )
    public Response insert(Ingrediente ingrediente) {
        Ingrediente.persist(ingrediente);
        return Response.status(201).entity(rep(ingrediente)).build();
    }

    @DELETE
    @RequiresApiKey
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Delete (Deleta um ingrediente)",
            description = "Deleta um ingrediente. O ingrediente não pode ser excluído se estiver sendo utilizada por alguma receita.")
    @Parameter(
            name = "X-API-Key",
            in = ParameterIn.HEADER,
            description = "Chave da API para autenticação"
    )
    public Response delete(@PathParam("id") long id) {
        Ingrediente entity = Ingrediente.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        Ingrediente.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @RequiresApiKey
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Update (Atualiza um ingrediente)",
            description = "Atualiza o nome de um ingrediente existente.")
    @Parameter(
            name = "X-API-Key",
            in = ParameterIn.HEADER,
            description = "Chave da API para autenticação"
    )
    public Response update(
            @Parameter(description = "Id do ingrediente", required = true)
            @PathParam("id") long id,
            @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Ingrediente.class, example = "{\"nome\": \"Batata\"}")
                    )
            )
            Ingrediente newIngrediente) {
        Ingrediente entity = Ingrediente.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }

        entity.nome = newIngrediente.nome;

        return Response.status(200).entity(rep(entity)).build();
    }

    public Response fallbackParaListarIngredientes() {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity(Map.of("erro", "Taxa de requisições excedida. Tente novamente mais tarde."))
                .header("X-RateLimit-Limit", 15)
                .header("X-RateLimit-Remaining", 0)
                .build();
    }

    public Response fallbackParaBuscaDeIngredientes(String q, String sort, String direction, int page, int size) {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity(Map.of("erro", "Taxa de requisições excedida. Tente novamente mais tarde."))
                .header("X-RateLimit-Limit", 20)
                .header("X-RateLimit-Remaining", 0)
                .build();
    }

    public Response fallbackParaIngredientePorId(long id) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("erro", "Serviço indisponível para o ingrediente " + id + ". Tente novamente."))
                .build();
    }
}