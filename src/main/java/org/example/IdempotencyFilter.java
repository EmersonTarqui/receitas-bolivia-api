package org.example;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;
import java.time.Instant;

@Provider
@ApplicationScoped
@Priority(Priorities.HEADER_DECORATOR)
public class IdempotencyFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private static final String IDEMPOTENT_CONTEXT_PROPERTY = "idempotent-context";

    @Inject
    @CacheName("idempotency-cache")
    Cache cache;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Verifica se o método/classe tem a anotação @Idempotent
        Method method = resourceInfo.getResourceMethod();
        Class<?> clazz = resourceInfo.getResourceClass();

        Idempotent methodAnnotation = method.getAnnotation(Idempotent.class);
        Idempotent classAnnotation = clazz.getAnnotation(Idempotent.class);

        if (methodAnnotation == null && classAnnotation == null) {
            // Endpoint não requer idempotência
            return;
        }

        // Usa a anotação do método, se existir, senão usa a da classe
        Idempotent idempotentConfig = methodAnnotation != null ? methodAnnotation : classAnnotation;

        // Obtém a chave de idempotência do cabeçalho
        String idempotencyKey = requestContext.getHeaderString(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // Chave de idempotência não fornecida
            requestContext.abortWith(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("O cabeçalho X-Idempotency-Key é obrigatório para esta operação")
                    .build());
            return;
        }

        // Cria o identificador único para esta requisição
        String cacheKey = createCacheKey(requestContext, idempotencyKey);

        try {
            // Obtém o cache de forma síncrona
            IdempotencyRecord record = (IdempotencyRecord) cache.get(cacheKey, k -> null)
                    .await().indefinitely();

            if (record != null) {
                // A requisição já foi processada, retorna o resultado cacheado
                requestContext.abortWith(Response
                        .status(record.getStatus())
                        .entity(record.getBody())
                        .build());
                return;
            }

            // Armazena o contexto para uso no filtro de resposta
            requestContext.setProperty(IDEMPOTENT_CONTEXT_PROPERTY,
                    new IdempotentContext(cacheKey, idempotentConfig.expireAfter()));

        } catch (Exception e) {
            // Em caso de erro, permite a requisição continuar (fail-open)
            // Pode ser alterado para fail-closed se necessário
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        IdempotentContext context = (IdempotentContext) requestContext.getProperty(IDEMPOTENT_CONTEXT_PROPERTY);
        if (context == null) {
            // Não é uma requisição idempotente
            return;
        }

        // Cria um registro para armazenar no cache
        IdempotencyRecord record = new IdempotencyRecord(
                responseContext.getStatus(),
                responseContext.getEntity(),
                Instant.now().plusSeconds(context.getExpireAfter())
        );

        try {
            // Armazena o resultado no cache
            cache.invalidate(context.getCacheKey()).await().indefinitely();
            cache.get(context.getCacheKey(), k -> record).await().indefinitely();
        } catch (Exception e) {
            // Ignora falhas no cache (fail-open)
        }
    }

    private String createCacheKey(ContainerRequestContext requestContext, String idempotencyKey) {
        // Combina método, caminho e chave de idempotência para criar uma chave única
        return requestContext.getMethod() + ":" +
                requestContext.getUriInfo().getPath() + ":" +
                idempotencyKey;
    }

    // Classe para armazenar o contexto entre os filtros de requisição e resposta
    private static class IdempotentContext {
        private final String cacheKey;
        private final int expireAfter;

        public IdempotentContext(String cacheKey, int expireAfter) {
            this.cacheKey = cacheKey;
            this.expireAfter = expireAfter;
        }

        public String getCacheKey() {
            return cacheKey;
        }

        public int getExpireAfter() {
            return expireAfter;
        }
    }

    // Classe para armazenar o resultado de uma requisição idempotente
    private static class IdempotencyRecord {
        private final int status;
        private final Object body;
        private final Instant expiry;

        public IdempotencyRecord(int status, Object body, Instant expiry) {
            this.status = status;
            this.body = body;
            this.expiry = expiry;
        }

        public int getStatus() {
            return status;
        }

        public Object getBody() {
            return body;
        }

        public Instant getExpiry() {
            return expiry;
        }
    }
}
