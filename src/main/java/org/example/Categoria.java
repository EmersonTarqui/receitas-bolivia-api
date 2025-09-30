package org.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Schema(description = "Representa uma categoria para as receitas (ex: Sopas, Sobremesas).")

public class Categoria extends PanacheEntity {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
    @Schema(description = "Nome da categoria", example = "Sopas")
    public String nome;
}