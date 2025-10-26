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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Categorias", description = "Operações relacionadas a categorias")
public class CategoriaResource {

    @Context
    UriInfo uriInfo;

    private CategoriaRepresentation rep(Categoria c) {
        return CategoriaRepresentation.from(c, uriInfo);
    }

    private List<CategoriaRepresentation> repList(List<Categoria> categorias) {
        return categorias.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(
            summary = "getAll (Retorna todas as categorias)",
            description = "Retorna uma lista de categorias por padrão no formato JSON"
    )
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categoria.class, type = SchemaType.ARRAY)
            )
    )
    public Response getAll() {
        return Response.ok(repList(Categoria.listAll())).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "getById (Busca uma categoria por ID)")
    public Response getById(
            @Parameter(description = "Id da categoria a ser pesquisada", required = true)
            @PathParam("id") long id) {
        Categoria entity = Categoria.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search")
    public Response search(
            @Parameter(description = "Query de busca por nome da categoria")
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

        PanacheQuery<Categoria> query = (q == null || q.isBlank())
                ? Categoria.findAll(sortObj)
                : Categoria.find("lower(nome) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Categoria> categorias = query.page(effectivePage, size).list();

        SearchCategoriaResponse response = SearchCategoriaResponse.from(
                categorias, uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );

        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Insert (Cria uma nova categoria)")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categoria.class,
                    example = "{\"nome\": \"Sobremesas\"}"
                    )
            )
    )
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categoria.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class))
    )
    public Response insert(Categoria categoria) {
        Categoria.persist(categoria);
        return Response.status(201).entity(rep(categoria)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Delete (Deleta uma categoria)",
    description = "Deleta uma categoria. A categoria não pode ser excluída se estiver sendo utilizada por alguma receita.")
    public Response delete(@PathParam("id") long id) {
        Categoria entity = Categoria.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        Categoria.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Update (Atualiza uma categoria)",
    description = "Atualiza o nome de uma categoria existente.")
    public Response update(
            @Parameter(description = "Id da categoria a ser atualizada", required = true)
            @PathParam("id") long id,
            @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categoria.class, example = "{\"nome\": \"Bebidas\"}")
                    )
            )
            Categoria newCategoria) {
        Categoria entity = Categoria.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }

        entity.nome = newCategoria.nome;

        return Response.status(200).entity(rep(entity)).build();
    }
}

