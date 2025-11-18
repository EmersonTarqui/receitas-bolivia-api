package org.example;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.Receita.Dificuldade;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/v2/receitas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "ReceitasV2", description = "Operações relacionadas a receitas (V2)")
public class ReceitaResourceV2 {

    @GET
    @Operation(summary = "Listar receitas com filtros (V2)",
            description = "Recupera receitas com filtros, ordenação e paginação.")
    public Response getAllV2(
            @QueryParam("nome") String nome,
            @QueryParam("origem") String origem,
            @QueryParam("dificuldade") Dificuldade dificuldade,
            @QueryParam("ordenar") @DefaultValue("id") String ordenarPor,
            @QueryParam("ordem") @DefaultValue("asc") String ordem,
            @QueryParam("pagina") @DefaultValue("1") @Min(1) int pagina,
            @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {

        // Construir a query dinâmica
        StringBuilder query = new StringBuilder("SELECT r FROM Receita r WHERE 1=1");
        Map<String, Object> parametros = new HashMap<>();

        // Aplicar filtros condicionalmente
        if (nome != null && !nome.trim().isEmpty()) {
            query.append(" AND LOWER(r.nome) LIKE LOWER(:nome)");
            parametros.put("nome", "%" + nome + "%");
        }

        if (origem != null && !origem.trim().isEmpty()) {
            query.append(" AND LOWER(r.origem) = LOWER(:origem)");
            parametros.put("origem", origem);
        }

        if (dificuldade != null) {
            query.append(" AND r.dificuldade = :dificuldade");
            parametros.put("dificuldade", dificuldade);
        }

        // Aplicar ordenação
        query.append(" ORDER BY r.").append(sanitizarCampoOrdenacao(ordenarPor))
                .append(" ").append(sanitizarOrdem(ordem));

        //  Executar a query com paginação
        PanacheQuery<Receita> panacheQuery = Receita.find(query.toString(), parametros);

        //  Aplicar paginação
        List<Receita> receitas = panacheQuery.page(pagina - 1, tamanho).list();
        long totalItens = panacheQuery.count();
        int totalPaginas = (int) Math.ceil((double) totalItens / tamanho);

        // Criar resposta com metadados de paginação
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("receitas", receitas);
        resposta.put("pagina_atual", pagina);
        resposta.put("tamanho_pagina", tamanho);
        resposta.put("total_itens", totalItens);
        resposta.put("total_paginas", totalPaginas);

        return Response.ok(resposta).build();
    }

    private String sanitizarCampoOrdenacao(String campo) {
        // Lista de campos permitidos para ordenação
        Set<String> camposPermitidos = Set.of("id", "nome", "origem", "dificuldade");
        return camposPermitidos.contains(campo) ? campo : "id";
    }

    private String sanitizarOrdem(String ordem) {
        return "desc".equalsIgnoreCase(ordem) ? "DESC" : "ASC";
    }
}