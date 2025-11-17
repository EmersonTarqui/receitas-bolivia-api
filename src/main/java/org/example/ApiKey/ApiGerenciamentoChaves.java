package org.example.ApiKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.ApiKey.ApiKey;

@Path("/v1/gerar-chave")
@Tag(name = "Chaves", description = "Gerenciamento de autenticação")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ApiGerenciamentoChaves {

    @POST
    @Transactional
    public Response gerarChave(@QueryParam("usuario") String usuario) {

        if (usuario == null || usuario.isBlank()) {
            return Response.status(400)
                    .entity("Precisa informar o 'usuario'")
                    .build();
        }

        ApiKey novaChave = new ApiKey();
        novaChave.username = usuario;
        novaChave.chave = "key_" + UUID.randomUUID().toString();
        novaChave.ativa = true;

        novaChave.persist();

        return Response.status(Response.Status.CREATED)
                .entity(novaChave)
                .build();
    }
}