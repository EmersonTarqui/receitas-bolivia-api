package org.example;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ReceitaRepresentation {

    public Long id;
    public String nome;
    public String origem;
    public Receita.Dificuldade dificuldade;
    public String categoria;
    public List<String> ingredientes;
    public List<String> modoPreparo;
    public Integer calorias;
    public String porcaoDescricao;
    public Double carboidratos;

    public Map<String, String> _links;

    public ReceitaRepresentation() {
    }

    public static ReceitaRepresentation from(Receita receita, UriInfo uriInfo) {
        ReceitaRepresentation rep = new ReceitaRepresentation();
        rep.id = receita.id;
        rep.nome = receita.nome;
        rep.origem = receita.origem;
        rep.modoPreparo = receita.modoPreparo;
        rep.dificuldade = receita.dificuldade;

        if (receita.categoria != null) {
            rep.categoria = receita.categoria.nome;
        }

        if (receita.ingredientes != null) {
            rep.ingredientes = receita.ingredientes.stream()
                    .map(ingrediente -> ingrediente.nome)
                    .collect(Collectors.toList());
        } else {
            rep.ingredientes = new ArrayList<>();
        }

        if (receita.informacaoNutricional != null) {
            rep.calorias = receita.informacaoNutricional.calorias;
            rep.porcaoDescricao = receita.informacaoNutricional.porcaoDescricao;
            rep.carboidratos = receita.informacaoNutricional.carboidratos;
        }

        rep._links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();
        rep._links.put("self", baseUri + "receitas/" + receita.id);
        rep._links.put("all", baseUri + "receitas");
        rep._links.put("delete", baseUri + "receitas/" + receita.id);
        rep._links.put("update", baseUri + "receitas/" + receita.id);
        rep._links.put("search", baseUri + "receitas/search");

        return rep;
    }
}

