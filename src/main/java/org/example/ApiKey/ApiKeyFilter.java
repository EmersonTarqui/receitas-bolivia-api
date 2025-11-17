package org.example.ApiKey;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.ApiKey.ApiKey;
import org.example.ApiKey.RequiresApiKey;

@Provider
@RequiresApiKey
@ApplicationScoped
public class ApiKeyFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "quarkus.api-key.header-name", defaultValue = "X-API-Key")
    String apiKeyHeader;

    @Override
    @Transactional
    public void filter(ContainerRequestContext requestContext) {

        // Verificar se é uma rota pública (opcional)
        // if (isPublicRoute(requestContext.getUriInfo().getPath())) {
        //    return;
        //}

        // Obter o valor do cabeçalho da API key
        String chaveRecebida =
                requestContext.getHeaderString(apiKeyHeader);

        // Verificar se a API key é válida
        if (chaveRecebida == null) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                    .entity( "API key inválida ou ausente")
                    .build());
            return;
        }

        //  buscar chave no banco
        ApiKey apiKey = ApiKey.buscarChave(chaveRecebida);

        if (apiKey == null || !apiKey.ativa) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("API key inválida")
                            .build());
            return;
        }
    }

 /*   private boolean isPublicRoute(String path) {
        // Defina aqui suas rotas públicas que não requerem autenticação
        return path.contains("/public/") || path.startsWith("/health")
                || path.startsWith("/metrics")
                || path.startsWith("/gerar-chave");
    }*/
}