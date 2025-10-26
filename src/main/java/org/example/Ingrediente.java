package org.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Schema(description = "Representa um ingrediente que pode ser usado em várias receitas.")
public class Ingrediente extends PanacheEntity {

    @NotBlank(message = "O nome do ingrediente é obrigatório")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    @Schema(description = "Nome do ingrediente", example = "Amendoim")
    public String nome;
}