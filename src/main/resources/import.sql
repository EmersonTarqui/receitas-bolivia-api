-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;


-- Categorias
INSERT INTO Categoria (id, nome) VALUES (1, 'Sopas');
INSERT INTO Categoria (id, nome) VALUES (2, 'Salgados');
INSERT INTO Categoria (id, nome) VALUES (3, 'Sobremesas');

-- Ingredientes
INSERT INTO Ingrediente (id, nome) VALUES (1, 'Amendoim Cru');
INSERT INTO Ingrediente (id, nome) VALUES (2, 'Costela de Boi');
INSERT INTO Ingrediente (id, nome) VALUES (3, 'Frango');

-- Sopa de Maní
INSERT INTO InformacaoNutricional (id, calorias, porcaoDescricao, carboidratos) VALUES (1, 450, 'por porção individual', 35.5);
INSERT INTO Receita (id, nome, origem, dificuldade, categoria_id, info_nutricional_id) VALUES (1, 'Sopa de Maní', 'Cochabamba, Bolívia', 'MEDIO', 1, 1);
INSERT INTO Receita_modoPreparo (Receita_id, modoPreparo) VALUES (1, 'Cozinhe a costela até ficar macia.');
INSERT INTO Receita_modoPreparo (Receita_id, modoPreparo) VALUES (1, 'Bata o amendoim no liquidificador com um pouco de caldo.');
INSERT INTO Receita_Ingrediente (receita_id, ingrediente_id) VALUES (1, 1);
INSERT INTO Receita_Ingrediente (receita_id, ingrediente_id) VALUES (1, 2);

--Salteña
INSERT INTO InformacaoNutricional (id, calorias, porcaoDescricao, carboidratos) VALUES (2, 380, 'por unidade', 45.0);
INSERT INTO Receita (id, nome, origem, dificuldade, categoria_id, info_nutricional_id) VALUES (2, 'Salteña', 'Potosí, Bolívia', 'DIFICIL', 2, 2);
INSERT INTO Receita_modoPreparo (Receita_id, modoPreparo) VALUES (2, 'Prepare o recheio (jigote) e deixe esfriar completamente.');
INSERT INTO Receita_Ingrediente (receita_id, ingrediente_id) VALUES (2, 3);


--alter sequence pra evitar conflito de id ao iniciar a aplicação

-- prox Categoria_id vai ser 4
ALTER SEQUENCE Categoria_SEQ RESTART WITH 4;
-- prox Ingrediente_id vai ser 4
ALTER SEQUENCE Ingrediente_SEQ RESTART WITH 4;
-- prox InformacaoNutricional_id é 3
ALTER SEQUENCE InformacaoNutricional_SEQ RESTART WITH 3;
--prox Receita_id vai ser 3
ALTER SEQUENCE Receita_SEQ RESTART WITH 3;
