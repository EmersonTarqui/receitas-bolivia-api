# API de Receitas Bolivianas

API desenvolvida para a disciplina de Web Services utilizando Quarkus. Fornecendo funcionalidades simples para o gerenciamento de receitas, categorias e ingredientes da culinária boliviana.

## Tecnologias Utilizadas

* **Quarkus** - Framework Java para desenvolvimento nativo em nuvem.
* **Java / Maven** - Linguagem e gerenciador de dependências.
* **Hibernate ORM com Panache** - Para facilitar a persistência de dados.
* **OpenAPI / Swagger-UI** - Para documentação da API.

## Funcionalidades

### Recursos Disponíveis

* 🍲 **Receitas:** Cadastro, consulta, atualização e exclusão de receitas.
* 🏷️ **Categorias:** Gerenciamento completo de categorias para as receitas.
* 🥕 **Ingredientes:** Administração dos ingredientes que podem ser utilizados nos pratos.

### Endpoints Principais

#### Receitas (`/receitas`)

* `GET /receitas` - Lista todas as receitas.
* `POST /receitas` - Cria uma nova receita.
* `GET /receitas/{id}` - Retorna informações de uma receita específica.
* `PUT /receitas/{id}` - Atualiza os dados de uma receita.
* `DELETE /receitas/{id}` - Exclui uma receita.
* `GET /receitas/search` - Pesquisa receitas com filtros e paginação.

#### Categorias (`/categorias`)

* `GET /categorias` - Lista todas as categorias.
* `POST /categorias` - Cria uma nova categoria.
* `GET /categorias/{id}` - Retorna uma categoria específica.
* `PUT /categorias/{id}` - Atualiza uma categoria.
* `DELETE /categorias/{id}` - Exclui uma categoria.
* `GET /categorias/search` - Pesquisa categorias com filtros e paginação.

#### Ingredientes (`/ingredientes`)

* `GET /ingredientes` - Lista todos os ingredientes.
* `POST /ingredientes` - Cria um novo ingrediente.
* `GET /ingredientes/{id}` - Retorna um ingrediente específico.
* `PUT /ingredientes/{id}` - Atualiza um ingrediente.
* `DELETE /ingredientes/{id}` - Exclui um ingrediente.
* `GET /ingredientes/search` - Pesquisa ingredientes com filtros e paginação.

## Documentação da API

Após executar a aplicação em modo de desenvolvimento, a documentação interativa pode ser acessada em:

* **Swagger UI:** https://receitas-bolivia-api.onrender.com/q/swagger-ui/

## Desenvolvimento

### Principais Entidades

* **Receita**: Representa o prato principal e conecta todas as outras entidades.
* **Categoria**: Representa o tipo de prato.
* **Ingrediente**: Representa os ingredientes que podem ser usados nas receitas.
* **CustoEstimado**: Representa o custo, as porções e a moeda para o preparo de uma receita.
