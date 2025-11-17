package org.example.ApiKey;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

@Entity
public class ApiKey extends PanacheEntity {

    @NotBlank
    public String chave;

    @NotBlank
    public String username;
    public boolean ativa;

    public static ApiKey buscarChave(String key) {
        return find("chave", key).firstResult();
    }
}

