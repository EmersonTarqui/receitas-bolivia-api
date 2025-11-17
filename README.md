# API de Receitas Bolivianas

API desenvolvida para a disciplina de Web Services utilizando Quarkus, que fornece várias funcionalidades para o gerenciamento de receitas, categorias e ingredientes da culinária boliviana.
## Tecnologias Utilizadas

* **Quarkus** - Framework Java para desenvolvimento nativo em nuvem.
* **Java / Maven** - Linguagem e gerenciador de dependências.
* **Hibernate ORM com Panache** - Para facilitar a persistência de dados.
* **SmallRye Fault Tolerance** - Para resiliência (Rate Limit, Fallback, Retry).
* **OpenAPI / Swagger-UI** - Para documentação da API.

## Funcionalidades

A API está versionada, com a V1 contendo os endpoints principais e a V2 introduzindo novos recursos de consulta.

### Endpoints V1 (`/v1`)

#### Receitas (`/v1/receitas`)

* `GET /v1/receitas` - Lista todas as receitas. (Resposta: `200 OK`)
* `POST /v1/receitas` - Cria uma nova receita. (Resposta: `201 Created`. Requer `X-API-Key` e `X-Idempotency-Key`)
* `GET /v1/receitas/{id}` - Retorna uma receita específica. (Resposta: `200 OK` ou `404 Not Found`)
* `PUT /v1/receitas/{id}` - Atualiza os dados de uma receita. (Resposta: `200 OK`. Requer `X-API-Key`)
* `DELETE /v1/receitas/{id}` - Exclui uma receita. (Resposta: `204 No Content`. Requer `X-API-Key`)
* `GET /v1/receitas/search` - Pesquisa receitas. (Resposta: `200 OK`)

#### Categorias (`/v1/categorias`)

* `GET /v1/categorias` - Lista todas as categorias. (Resposta: `200 OK`)
* `POST /v1/categorias` - Cria uma nova categoria. (Resposta: `201 Created`. Requer `X-API-Key` e `X-Idempotency-Key`)
* `GET /v1/categorias/{id}` - Retorna uma categoria específica. (Resposta: `200 OK` ou `404 Not Found`)
* `PUT /v1/categorias/{id}` - Atualiza uma categoria. (Resposta: `200 OK`. Requer `X-API-Key`)
* `DELETE /v1/categorias/{id}` - Exclui uma categoria. (Resposta: `204 No Content`. Requer `X-API-Key`)
* `GET /v1/categorias/search` - Pesquisa categorias. (Resposta: `200 OK`)

#### Ingredientes (`/v1/ingredientes`)

* `GET /v1/ingredientes` - Lista todos os ingredientes. (Resposta: `200 OK`)
* `POST /v1/ingredientes` - Cria um novo ingrediente. (Resposta: `201 Created`. Requer `X-API-Key` e `X-Idempotency-Key`)
* `GET /v1/ingredientes/{id}` - Retorna um ingrediente específico. (Resposta: `200 OK` ou `404 Not Found`)
* `PUT /v1/ingredientes/{id}` - Atualiza um ingrediente. (Resposta: `200 OK`. Requer `X-API-Key`)
* `DELETE /v1/ingredientes/{id}` - Exclui um ingrediente. (Resposta: `204 No Content`. Requer `X-API-Key`)
* `GET /v1/ingredientes/search` - Pesquisa ingredientes. (Resposta: `200 OK`)

### Endpoints V2 (`/v2`)

#### Receitas (`/v2/receitas`)

* `GET /v2/receitas` - (Upgrade) Lista receitas com filtros avançados (nome, origem, dificuldade), ordenação e paginação. (Resposta: `200 OK` com dados paginados)

### Gerenciamento de Acesso

* `POST /v1/gerar-chave` - Endpoint público para gerar uma nova `X-API-Key`. (Ex: `?usuario=meu_nome`). (Resposta: `201 Created` com a nova chave)

## Padrões de API Implementados

* **Autenticação via API Key:** Endpoints "sensíveis" (POST, PUT, DELETE) são protegidos com `@RequiresApiKey`.
    * **Exige (Header):** `X-API-Key: key_...` (chave válida gerada pelo endpoint de gerenciamento).
    * **Resposta de Falha (Header Ausente):** `401 Unauthorized` com JSON `{"erro": "O cabeçalho X-API-Key é obrigatório."}`.
    * **Resposta de Falha (Chave Inválida):** `401 Unauthorized` com JSON `{"erro": "A chave de API (API key) fornecida é inválida ou inativa."}`.

* **Idempotência:** Endpoints `POST` (criação) utilizam um header `X-Idempotency-Key`.
    * **Exige (Header):** `X-Idempotency-Key: <uuid-unico-por-requisicao>`
    * **Funcionamento:** Se a mesma requisição for enviada duas vezes, o `IdempotencyFilter` previne a criação duplicada e retorna a resposta original (`201 Created`) que foi salva em cache.
    * **Resposta de Falha (Header Ausente):** `400 Bad Request` com JSON `{"erro": "O cabeçalho X-Idempotency-Key é obrigatório para esta operação."}`.

* **Rate Limiting:** Endpoints `GET` públicos possuem um limite de requisições (ex: 15/min).
    * **Resposta de Falha:** `429 Too Many Requests` (com JSON `{"erro": "Taxa de requisições excedida..."}`).
    * **Headers de Resposta (na falha):** `X-RateLimit-Limit: 15` e `X-RateLimit-Remaining: 0`.

* **Versionamento (via URL):** A API é versionada (ex: `/v1/` e `/v2/`) para permitir upgrades (como o filtro V2) sem quebrar clientes antigos.

* **CORS:** Configurado no `application.properties` para permitir que outros sites acessem a API e para aceitar cabeçalhos (headers) personalizados como `X-API-Key` e `X-Idempotency-Key`.

## Documentação da API

Após executar a aplicação, a documentação interativa pode ser acessada em:

* **Swagger UI:** https://receitas-bolivia-api.onrender.com/q/swagger-ui/

## Desenvolvimento

### Principais Entidades

* **Receita**: Entidade principal que conecta as demais.
* **Categoria**: O tipo de prato (ex: Sopas, Salgados).
* **Ingrediente**: Os ingredientes usados nas receitas.
* **InformacaoNutricional**: Informações nutricionais de uma receita.
* **ApiKey**: Armazena as chaves de API (`X-API-Key`) para autenticação dos usuários.