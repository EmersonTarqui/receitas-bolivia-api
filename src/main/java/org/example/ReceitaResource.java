package org.example;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
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

@Path("/receitas")
@Tag(name = "Receitas", description = "Operações relacionadas a receitas")
public class ReceitaResource {

    @Context
    UriInfo uriInfo;

    private ReceitaRepresentation rep(Receita r) {
        return ReceitaRepresentation.from(r, uriInfo);
    }

    private List<ReceitaRepresentation> repList(List<Receita> receitas) {
        return receitas.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(
            summary = "getAll (Retorna todas as receitas)",
            description = "Retorna uma lista de receitas por padrão no formato JSON"
    )
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Receita.class, type = SchemaType.ARRAY)
            )
    )
    public Response getAll() {
        return Response.ok(repList(Receita.listAll())).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "getById (Busca uma receita por ID)")
    public Response getById(
            @Parameter(description = "Id da receita a ser pesquisada", required = true)
            @PathParam("id") long id) {
        Receita entity = Receita.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search")
    @Transactional
    public Response search(
            @Parameter(description = "Query de busca por nome ou origem") @QueryParam("q") String q,
            @Parameter(description = "Campo de ordenação") @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação") @QueryParam("direction") @DefaultValue("asc") String direction,
            @Parameter(description = "Página da busca") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Quantidade de itens por página") @QueryParam("size") @DefaultValue("10") int size) {

        Set<String> allowed = Set.of("id", "nome", "origem", "dificuldade");
        if (!allowed.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Receita> query = (q == null || q.isBlank())
                ? Receita.findAll(sortObj)
                : Receita.find("lower(nome) like ?1 or lower(origem) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Receita> receitas = query.page(effectivePage, size).list();

        SearchReceitaResponse response = SearchReceitaResponse.from(receitas, uriInfo, q, sort, direction, page, size, totalElements, totalPages);

        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Insert (Cria uma nova receita)")
    @RequestBody(
            description = "Para criar uma receita, é necessário que a Categoria e os Ingredientes já existam. Envie os IDs correspondentes.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Receita.class,
                            example = "{\"nome\": \"Pique a lo Macho\", " +
                                    "\"origem\": \"Cochabamba, Bolívia\", " +
                                    "\"dificuldade\": \"MEDIO\", " +
                                    "\"modoPreparo\": [" +
                                    "\"Corte a carne em pedaços e frite em uma panela grande com um pouco de óleo.\", " +
                                    "\"Adicione a salsicha em rodelas, a cebola e o tomate picados. Cozinhe até os vegetais ficarem macios.\", " +
                                    "\"Frite as batatas separadamente até ficarem douradas e crocantes.\", " +
                                    "\"Cozinhe os ovos, descasque e corte-os em rodelas.\", " +
                                    "\"Em uma travessa grande, coloque as batatas fritas como base, adicione a mistura de carne, salsicha e vegetais por cima.\", " +
                                    "\"Decore com as rodelas de ovos cozidos.\", " +
                                    "\"Sirva imediatamente, bem quente, acompanhado do molho apimentado (llajwa) se desejar.\"" +
                                    "], " +
                                    "\"informacaoNutricional\": {\"calorias\": 800, \"porcaoDescricao\": \"por prato\", \"carboidratos\": 50.0}, " +
                                    "\"categoria\": {\"id\": 1}, " +
                                    "\"ingredientes\": [{\"id\": 1}, {\"id\": 2}]}"
                    )
            )
    )
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Receita.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class))
    )
    public Response insert(Receita receita) {
        Receita.persist(receita);
        return Response.status(201).entity(rep(receita)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Delete (Deleta uma receita)")
    public Response delete(@PathParam("id") long id) {
        Receita entity = Receita.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        Receita.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Update (Atualiza uma receita)",
            description = "Atualiza uma receita  existente.")
    public Response update(
            @Parameter(description = "Id da receita a ser atualizada", required = true)
                                   @PathParam("id") long id,
                               @RequestBody(
                                       required = true,
                                       content = @Content(
                                               mediaType = "application/json",
                                               schema = @Schema(implementation = Receita.class,
                                                       example = "{\"nome\": \"Tucumana\", " +
                                                               "\"origem\": \"Bolívia\", " +
                                                               "\"dificuldade\": \"DIFICIL\", " +
                                                               "\"modoPreparo\": [" +
                                                               "\"Para o recheio, pique a carne em cubos pequenos e refogue com cebola, pimentão e temperos como cominho e ají panca.\", " +
                                                               "\"Adicione batatas cozidas picadas, ervilhas, azeitonas e ovos cozidos fatiados ao recheio e misture bem.\", " +
                                                               "\"Para a massa, misture farinha de trigo, sal e manteiga. Adicione água aos poucos até formar uma massa macia.\", " +
                                                               "\"Abra a massa em círculos de aproximadamente 10 cm, recheie com a mistura e feche bem, formando um 'repulgue'.\", " +
                                                               "\"Frite as tucumanas em óleo bem quente até ficarem douradas e crocantes por fora.\" " +
                                                               "], " +
                                                               "\"informacaoNutricional\": {\"calorias\": 900, \"porcaoDescricao\": \"por prato\", \"carboidratos\": 55.0}, "  +
                                                               "\"categoria\": {\"id\": 2}, " +
                                                               "\"ingredientes\": [{\"id\": 1}, {\"id\": 2}]}")
                                       )
                               )  Receita newReceita) {
        Receita entity = Receita.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }

        entity.nome = newReceita.nome;
        entity.origem = newReceita.origem;
        entity.dificuldade = newReceita.dificuldade;
        entity.modoPreparo = newReceita.modoPreparo;
        entity.informacaoNutricional = newReceita.informacaoNutricional;
        entity.categoria = newReceita.categoria;
        entity.ingredientes = newReceita.ingredientes;

        return Response.status(200).entity(rep(entity)).build();
    }
}

