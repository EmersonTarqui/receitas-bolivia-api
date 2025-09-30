package org.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;

@Entity
@Schema(description = "Representa uma receita culinária boliviana.")
public class Receita extends PanacheEntity {

    public enum Dificuldade {
        FACIL,
        MEDIO,
        DIFICIL
    }

    @NotBlank(message = "O nome da receita é obrigatório")
    public String nome;

    @NotBlank(message = "A origem da receita é obrigatória")
    public String origem;

    @NotNull
    @Enumerated(EnumType.STRING)
    public Dificuldade dificuldade;

    @ElementCollection //relacao tabela a parte pelo id
    public List<String> modoPreparo;


    //Uma receita tem uma informacaoNutricional
    @OneToOne(cascade = CascadeType.ALL)//cascade = Se a receita for apagada, a InformacaoNutricional ligada vai embora junto
    @JoinColumn(name = "info_nutricional_id")
    public InformacaoNutricional informacaoNutricional;

    //muitas receitas para uma categoria
    @ManyToOne
    @NotNull(message = "A categoria é obrigatória")
    public Categoria categoria;

    //Muitas receitas usam muitos ingredientes
    @ManyToMany
    public List<Ingrediente> ingredientes;


    public Receita() {
    }
}