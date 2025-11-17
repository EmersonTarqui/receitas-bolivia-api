package org.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Schema(description = "Representa as informações nutricionais de uma receita.")
public class InformacaoNutricional extends PanacheEntity {

    @NotNull(message = "O valor das calorias é obrigatório.")
    @Schema(description = "Quantidade de calorias (kcal) por porção.", example = "450", minimum = "0")
    public Integer calorias;

    @NotBlank(message = "A descrição da porção é obrigatória.")
    @Schema(description = "Descrição da porção para os valores nutricionais.", example = "por porção individual", required = true)
    public String porcaoDescricao;

    @NotNull(message = "O valor de carboidratos é obrigatório.")
    @Schema(description = "Total de carboidratos (em grmamas).", example = "55.2", minimum= "0")
    public Double carboidratos;
}
