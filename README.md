# AutoBots — Automanager (ATVIII)

Microsserviço em **Java + Spring Boot** para lojas de manutenção veicular e venda de autopeças.

- **ATVI:** CRUD de clientes.
- **ATVII:** API nos quatro níveis do Modelo de Maturidade de Richardson, incluindo **HATEOAS**.
- **ATVIII:** a **"atualização de base"**. O sistema passa a cadastrar empresas, usuários (clientes, fornecedores e funcionários), credenciais, veículos, mercadorias, serviços e vendas. Todas as entidades têm **CRUD completo com HATEOAS**.

Atividade prática ATVIII — Prof. Dr. Eng. Gerson Penha.

> A "atualização de segurança" (login, autenticação, proteção de senha) é a **próxima** etapa e não faz parte desta entrega. Nesta etapa a credencial é apenas um cadastro.

---

## Sumário

1. [Requisitos](#1-requisitos)
2. [Como executar](#2-como-executar)
3. [Como saber se a aplicação está rodando](#3-como-saber-se-a-aplicação-está-rodando)
4. [Funcionalidades pedidas e onde estão](#4-funcionalidades-pedidas-e-onde-estão)
5. [Modelo de dados](#5-modelo-de-dados)
6. [HATEOAS: como funcionam os links](#6-hateoas-como-funcionam-os-links)
7. [Dados de exemplo](#7-dados-de-exemplo)
8. [Endpoints](#8-endpoints)
9. [Roteiro de teste rápido](#9-roteiro-de-teste-rápido)
10. [Banco de dados (H2 Console)](#10-banco-de-dados-h2-console)
11. [Alterações feitas no código-base](#11-alterações-feitas-no-código-base)
12. [Problemas comuns](#12-problemas-comuns)

---

## 1. Requisitos

| Item | Versão | Observação |
|---|---|---|
| **JDK (Java)** | **17 ou superior** | Testado com o **JDK 21**. Só o JRE não basta: é preciso o **JDK**. |
| Maven | *não precisa instalar* | O projeto traz o **Maven Wrapper** (`mvnw`), que baixa o Maven sozinho na primeira execução. |
| Internet | na 1ª execução | Para baixar as dependências. |
| Porta **8080** | livre | A aplicação sobe em `http://localhost:8080`. |
| Ferramenta HTTP | qualquer uma | Postman, Insomnia, Thunder Client (VS Code) ou `curl`. |

Tecnologias: Spring Boot 2.6.7 · Spring Data JPA · **Spring HATEOAS** · Banco H2 (em memória, não precisa instalar nada) · Lombok 1.18.34.

Para verificar o Java, digite `java -version` num terminal. A versão deve ser **17 ou maior**.

---

## 2. Como executar

O projeto fica na pasta **`automanager`** (onde está o `pom.xml`).

### Opção A — Pelo terminal

```
cd automanager
```

| Sistema | Comando |
|---|---|
| Windows (PowerShell) | `.\mvnw.cmd spring-boot:run` |
| Windows (cmd) | `mvnw.cmd spring-boot:run` |
| Linux / macOS | `./mvnw spring-boot:run` |

Para **parar**, pressione **Ctrl + C**.

### Opção B — Pelo Eclipse

> ⚠️ O Eclipse precisa do **plugin do Lombok**. Sem ele aparecem erros como `The method getNome() is undefined`.

1. **Instalar o Lombok** (uma vez): baixe o `lombok.jar` em <https://projectlombok.org/download>, execute-o (`java -jar lombok.jar`), selecione o Eclipse, clique em **Install / Update** e **reinicie o Eclipse**.
2. **Importar:** `File` → `Import...` → `Maven` → `Existing Maven Projects` → selecione a pasta **`automanager`** → `Finish`.
3. **Executar:** botão direito em `AutomanagerApplication.java` → `Run As` → `Java Application`.

Se houver erros: botão direito no projeto → `Maven` → `Update Project...` (Alt+F5).

### Opção C — Pelo VS Code

Instale o **Extension Pack for Java**, abra a pasta `automanager` e clique em **Run** acima do `main` de `AutomanagerApplication.java`.

---

## 3. Como saber se a aplicação está rodando

No final do log devem aparecer:

```
Tomcat started on port(s): 8080 (http)
Started AutomanagerApplication in X seconds
```

Abra no navegador <http://localhost:8080/empresa/1>. Deve aparecer a empresa de exemplo, com usuários, mercadorias, serviços e vendas, cada um com seus links.

---

## 4. Funcionalidades pedidas e onde estão

| Funcionalidade pedida pelos investidores | Como foi atendida |
|---|---|
| Cadastro de **empresa** (unidade comercial) | CRUD em `/empresa` |
| **Associação de usuários** a uma empresa | Todo usuário é cadastrado já dentro de uma empresa (`POST /usuario/cadastro/{empresaId}`). Um usuário pode ser transferido com `PUT /empresa/associar/{empresaId}/usuario/{usuarioId}` |
| **Tipos de usuário**: cliente, fornecedor ou funcionário | Campo `perfis` do usuário (`CLIENTE`, `FORNECEDOR`, `FUNCIONARIO`). O cadastro exige ao menos um perfil, e a venda confere os perfis (cliente precisa ser `CLIENTE`, funcionário precisa ser `FUNCIONARIO`) |
| Cadastro de **veículos, mercadorias e serviços** e gestão de **vendas** e serviços prestados a um usuário e seu veículo | CRUD em `/veiculo`, `/mercadoria`, `/servico` e `/venda`. A venda liga cliente, funcionário, veículo, mercadorias e serviços |
| **Credencial** de acesso do usuário | CRUD em `/credencial`, com dois tipos: usuário e senha, ou código de barras |
| CRUD e HATEOAS para todas as entidades | 11 entidades, todas com `GET`, `POST`, `PUT` e `DELETE` e links nas respostas |

---

## 5. Modelo de dados

Segue o diagrama de classes da atividade (Figura 1 do enunciado):

```
Empresa ─┬─ telefones, endereco
         ├─ usuarios ──┬─ telefones, endereco, documentos, emails, credenciais
         │             ├─ mercadorias   (mercadorias fornecidas, para FORNECEDOR)
         │             ├─ veiculos      (veículos do usuário, que é o proprietário)
         │             └─ vendas        (compras do usuário, para CLIENTE)
         ├─ mercadorias (estoque)
         ├─ servicos    (catálogo)
         └─ vendas ──── cliente, funcionario, veiculo, mercadorias, servicos
```

| Entidade | Pertence a | Observação |
|---|---|---|
| Empresa | — | Topo da hierarquia |
| Usuario | Empresa | Perfis: `CLIENTE`, `FORNECEDOR`, `FUNCIONARIO` |
| Credencial | Usuario | Abstrata. Tipos: `usuarioSenha` (nome de usuário + senha) e `codigoBarra` (código numérico) |
| Documento | Usuario | Tipos: `CPF`, `CNPJ`, `RG`, `CNH`, `PASSAPORTE`. Número **único** |
| Email | Usuario | |
| Telefone | Usuario **ou** Empresa | |
| Endereco | Usuario **ou** Empresa | No máximo um por dono |
| Veiculo | Usuario (proprietário) | Tipos: `HATCH`, `SEDA`, `SUV`, `PICKUP`, `SW` |
| Mercadoria | Empresa (estoque) e, opcionalmente, um fornecedor | |
| Servico | Empresa (catálogo) | |
| Venda | Empresa | Identificação **única**. A mesma mercadoria ou serviço pode aparecer em várias vendas |

> O diagrama mostra uma classe `Perfil` vazia, mas os perfis são representados pelo enum `PerfilUsuario` (que também está no diagrama, no campo `Set<PerfilUsuario> perfis`). Por isso a classe vazia não foi criada.

---

## 6. HATEOAS: como funcionam os links

Cada resposta indica, com links, para onde ir em seguida. Cada link tem um nome (*rel*) e um endereço (`href`).

| Resposta | Links |
|---|---|
| Um recurso (`GET /{id}`, `POST`, `PUT`) | `self` + link da coleção (ex.: `vendas`) |
| Uma lista (`GET` da coleção) | `self` em cada item |
| Itens dentro de um recurso | Também recebem `self`. Ex.: na venda, o cliente, o funcionário, o veículo, as mercadorias e os serviços apontam para o próprio endereço |
| `DELETE` | Sem links: `204` sem corpo |

### Exemplo: `GET /servico/1`

```json
{
  "id": 1,
  "nome": "Troca de rodas",
  "valor": 50.0,
  "descricao": "Troca das rodas do carro por novas",
  "_links": {
    "self":     { "href": "http://localhost:8080/servico/1" },
    "servicos": { "href": "http://localhost:8080/servico/servicos" }
  }
}
```

### Formato dos links nas listas

Nas listas, os links aparecem como `"links": [ { "rel": "self", "href": "..." } ]` em vez de `"_links": {...}`. O formato HAL (`_links`) só é usado quando o objeto devolvido é um `RepresentationModel`, e uma `List<...>` do Java não é. O conteúdo dos links é o mesmo. É o mesmo comportamento da ATVII.

### Relacionamentos nos dois sentidos

Alguns relacionamentos existem dos dois lados (usuário ↔ veículo ↔ venda). Sem cuidado, o JSON entraria em **loop infinito**: a venda mostra o cliente, que mostra as vendas, que mostram o cliente...

Para evitar isso, quando um objeto aparece **dentro** de outro, as listas que voltam para o objeto de origem são omitidas:

| Dentro de | Aparece | Sem as listas |
|---|---|---|
| Venda | `cliente`, `funcionario` | `vendas`, `veiculos`, `mercadorias` |
| Venda | `veiculo` | `vendas`, `proprietario` |
| Veiculo | `proprietario` | `vendas`, `veiculos`, `mercadorias` |

Para ver os dados completos, basta seguir o link `self` do item.

---

## 7. Dados de exemplo

O banco é **em memória**: começa vazio e é **apagado ao parar a aplicação**. A cada inicialização são cadastrados os dados abaixo, **sempre com os mesmos ids**:

| Registro | id | Detalhes |
|---|---|---|
| Empresa "Car service toyota ltda" | **1** | Endereço 1, telefone 1 |
| Usuário "Dom Pedro" — `FUNCIONARIO` | **1** | CPF (documento 1), email 1, credencial 1 (`dompedrofuncionario`), endereço 2, telefone 2 |
| Usuário "Loja do carro" — `FORNECEDOR` | **2** | CNPJ (documento 2), email 2, credencial 2 (`dompedrofornecedor`), endereço 3, fornece a mercadoria 1 |
| Usuário "Dom pedro cliente" — `CLIENTE` | **3** | CPF (documento 3), email 3, credencial 3 (`dompedrocliente`), endereço 4, dono do veículo 1, vendas 1 e 2 |
| Veículo Corolla Cross (SUV), placa `ABC-0000` | **1** | Proprietário: usuário 3 |
| Mercadoria "Roda de liga leve" | **1** | Estoque da empresa 1, fornecida pelo usuário 2 |
| Serviços | **1**, **2**, **3** | Troca de rodas, Alinhamento de rodas, Balanceamento de rodas |
| Venda `1234698745` | **1** | Cliente 3, funcionário 1, veículo 1, mercadoria 1, serviços 1 e 2 |
| Venda `1234698749` | **2** | Cliente 3, funcionário 1, veículo 1, mercadoria 1, serviços 2 e 3 |

---

## 8. Endpoints

URL base: `http://localhost:8080`. Em **POST** e **PUT**, envie JSON (`Content-Type: application/json`).

### Padrão das rotas

Todas as entidades seguem o mesmo padrão (`x` = nome da entidade):

| Método | Rota | Descrição |
|---|---|---|
| GET | `/x/xs` (plural) | Lista todos |
| GET | `/x/{id}` | Busca um |
| POST | `/x/cadastro/...` | Cadastra, já vinculado ao dono (tabela abaixo) |
| PUT | `/x/atualizar/{id}` | Atualiza **só os campos enviados**. Campos ausentes ou em branco são mantidos |
| DELETE | `/x/excluir/{id}` | Exclui |

> No cadastro, qualquer `id` enviado no corpo é **ignorado**: sempre se cria um registro novo.

### Códigos de resposta

| Código | Significado |
|---|---|
| `200 OK` | Consulta, atualização ou associação realizada |
| `201 Created` | Cadastro realizado. O corpo traz o registro criado, com id e links |
| `204 No Content` | Exclusão realizada |
| `400 Bad Request` | Campo obrigatório faltando, referência inexistente ou perfil errado |
| `404 Not Found` | Id da URL não existe |
| `409 Conflict` | Valor que deveria ser único repetido, endereço já cadastrado, ou exclusão que apagaria o histórico de vendas |

### 8.1 Rotas por entidade

| Entidade | Listar | Cadastrar | Obrigatórios no cadastro |
|---|---|---|---|
| Empresa | `/empresa/empresas` | `POST /empresa/cadastro` | `razaoSocial` |
| Usuário | `/usuario/usuarios` | `POST /usuario/cadastro/{empresaId}` | `nome`, ao menos um item em `perfis` |
| Credencial | `/credencial/credenciais` | `POST /credencial/cadastro/{usuarioId}` | `tipo` + (`nomeUsuario` e `senha`) ou `codigo` |
| Documento | `/documento/documentos` | `POST /documento/cadastro/{usuarioId}` | `tipo`, `numero`, `dataEmissao` |
| Email | `/email/emails` | `POST /email/cadastro/{usuarioId}` | `endereco` |
| Telefone | `/telefone/telefones` | `POST /telefone/cadastro/usuario/{usuarioId}` ou `POST /telefone/cadastro/empresa/{empresaId}` | `ddd`, `numero` |
| Endereço | `/endereco/enderecos` | `POST /endereco/cadastro/usuario/{usuarioId}` ou `POST /endereco/cadastro/empresa/{empresaId}` | todos, exceto `informacoesAdicionais` |
| Veículo | `/veiculo/veiculos` | `POST /veiculo/cadastro/{usuarioId}` (usuário = proprietário) | `tipo`, `modelo`, `placa` |
| Mercadoria | `/mercadoria/mercadorias` | `POST /mercadoria/cadastro/{empresaId}` | `nome`, `validade`, `fabricacao`, `quantidade`, `valor` |
| Serviço | `/servico/servicos` | `POST /servico/cadastro/{empresaId}` | `nome`, `valor` |
| Venda | `/venda/vendas` | `POST /venda/cadastro/{empresaId}` | `identificacao`, `cliente` |

Datas no formato `"2026-01-31"`. A data de cadastro (empresa, mercadoria) e a data de criação da credencial são preenchidas automaticamente.

### 8.2 Associações

| Método | Rota | Descrição |
|---|---|---|
| PUT | `/empresa/associar/{empresaId}/usuario/{usuarioId}` | Associa o usuário à empresa. Um usuário pertence a uma empresa só: se já estiver em outra, é **transferido**. Responde com a empresa |
| PUT | `/usuario/associar/{usuarioId}/mercadoria/{mercadoriaId}` | Define o usuário como fornecedor da mercadoria. Exige perfil `FORNECEDOR` e que fornecedor e mercadoria sejam da mesma empresa (senão `400`). Se a mercadoria já tinha outro fornecedor, é transferida. Responde com o fornecedor |

Outras ligações são feitas automaticamente:
- **Proprietário do veículo:** definido no cadastro do veículo.
- **Venda:** ao ser cadastrada, entra no histórico do **cliente** (`vendas` do usuário), do **veículo** (`vendas` do veículo) e da **empresa**.

### 8.3 Exemplos de corpo

**Empresa** (`POST /empresa/cadastro`). Telefones e endereço são opcionais:

```json
{
  "razaoSocial": "Volkswagen Serviços Ltda",
  "nomeFantasia": "VW Service",
  "telefones": [ { "ddd": "11", "numero": "40041234" } ],
  "endereco": { "estado": "SP", "cidade": "São Bernardo do Campo", "bairro": "Centro",
                "rua": "Via Anchieta", "numero": "1000", "codigoPostal": "09823-901" }
}
```

**Usuário** (`POST /usuario/cadastro/1`). Telefones, endereço, documentos, emails e credenciais podem ir junto:

```json
{
  "nome": "Ana Souza",
  "nomeSocial": "Ana",
  "perfis": [ "CLIENTE" ],
  "documentos": [ { "tipo": "CPF", "numero": "12345678901", "dataEmissao": "2010-05-20" } ],
  "emails": [ { "endereco": "ana@email.com" } ],
  "credenciais": [ { "tipo": "usuarioSenha", "nomeUsuario": "ana", "senha": "123456" } ]
}
```

> O usuário é atualizado em `PUT /usuario/atualizar/{id}` (nome, nome social, perfis e endereço). Telefones, documentos, emails e credenciais são alterados pelas rotas próprias de cada um.

**Credencial** (`POST /credencial/cadastro/3`). O campo **`tipo` é obrigatório**, porque `Credencial` é uma classe abstrata:

```json
{ "tipo": "usuarioSenha", "nomeUsuario": "anasouza", "senha": "abc123" }
```
```json
{ "tipo": "codigoBarra", "codigo": 7891234567890 }
```

> A `senha` é aceita no cadastro e na atualização, mas **nunca aparece nas respostas**.

No `PUT` da credencial, envie também o `tipo` (o mesmo da credencial). Ex.: para inativar: `{ "tipo": "usuarioSenha", "inativo": true }`.

**Veículo** (`POST /veiculo/cadastro/3`):

```json
{ "tipo": "HATCH", "modelo": "Polo", "placa": "BRA-2E19" }
```

**Mercadoria** (`POST /mercadoria/cadastro/1`):

```json
{ "nome": "Pastilha de freio", "fabricacao": "2026-01-10", "validade": "2030-01-10",
  "quantidade": 40, "valor": 120.0, "descricao": "Jogo dianteiro" }
```

**Serviço** (`POST /servico/cadastro/1`):

```json
{ "nome": "Troca de óleo", "valor": 80.0, "descricao": "Óleo sintético 5W30" }
```

**Venda** (`POST /venda/cadastro/1`). Os relacionados vão **só com o id**:

```json
{
  "identificacao": "VENDA-0003",
  "cliente":     { "id": 3 },
  "funcionario": { "id": 1 },
  "veiculo":     { "id": 1 },
  "mercadorias": [ { "id": 1 } ],
  "servicos":    [ { "id": 1 }, { "id": 3 } ]
}
```

Regras da venda (qualquer falha retorna `400` e nada é gravado):
- todo id informado precisa existir;
- o `cliente` precisa ter o perfil `CLIENTE`;
- o `funcionario` precisa ter o perfil `FUNCIONARIO`;
- o `veiculo` precisa ser do `cliente`;
- cliente, funcionário, mercadorias e serviços precisam ser **da empresa da venda** (a da URL no cadastro).

No `PUT` da venda, os relacionados enviados **substituem** os atuais. Os que não forem enviados são mantidos. O `PUT` é atômico: se qualquer regra falhar, nada é alterado. Para trocar o cliente de uma venda que tem veículo, envie também um veículo do novo cliente.

### 8.4 Exclusões

| O que se exclui | Efeito |
|---|---|
| Telefone, endereço, documento, email, credencial | Sai do dono e é apagado |
| Venda | Sai da empresa, do cliente e do veículo, e é apagada |
| Usuário | Sai da empresa e é apagado com seus telefones, documentos, emails, credenciais e endereço. **`409`** se for dono de veículo ou se participou de alguma venda |
| Veículo | **`409`** se participou de alguma venda |
| Mercadoria / Serviço | **`409`** se faz parte de alguma venda |
| Empresa | **`409`** enquanto tiver usuários ou vendas. Sem eles, é apagada com telefones, endereço, mercadorias e serviços |

O `409` protege o **histórico de vendas**. Para excluir algo que já foi vendido, exclua antes as vendas.

---

## 9. Roteiro de teste rápido

Com a aplicação **recém-iniciada** (dados da seção 7):

| # | Requisição | Corpo (JSON) | Resultado esperado |
|---|---|---|---|
| 1 | `GET /empresa/1` | — | `200`, empresa com 3 usuários, 1 mercadoria, 3 serviços e 2 vendas, todos com `self` |
| 2 | `GET /venda/1` | — | `200`, venda com cliente, funcionário, veículo, mercadoria e serviços, cada um com `self` |
| 3 | `GET /usuario/3` | — | `200`, cliente com o veículo 1 e as vendas 1 e 2 |
| 4 | `POST /usuario/cadastro/1` | `{"nome":"Ana","perfis":["CLIENTE"]}` | `201`, usuário id 4 |
| 5 | `POST /veiculo/cadastro/4` | `{"tipo":"HATCH","modelo":"Polo","placa":"BRA-2E19"}` | `201`, veículo id 2 com proprietário Ana |
| 6 | `POST /credencial/cadastro/4` | `{"tipo":"usuarioSenha","nomeUsuario":"ana","senha":"1"}` | `201`, credencial com `criacao` preenchida e `inativo: false` |
| 7 | `POST /venda/cadastro/1` | `{"identificacao":"V3","cliente":{"id":4},"funcionario":{"id":1},"veiculo":{"id":2},"mercadorias":[{"id":1}],"servicos":[{"id":1}]}` | `201`, venda id 3. A **mesma** mercadoria 1 já estava nas vendas 1 e 2 |
| 8 | `GET /usuario/4` | — | Ana com o veículo 2 e a venda 3 |
| 9 | `POST /venda/cadastro/1` | `{"identificacao":"V4","cliente":{"id":1}}` | `400`: o usuário 1 é funcionário, não cliente |
| 10 | `POST /venda/cadastro/1` | `{"identificacao":"V4","cliente":{"id":4},"veiculo":{"id":1}}` | `400`: o veículo 1 não é da Ana |
| 11 | `POST /venda/cadastro/1` | `{"identificacao":"V3","cliente":{"id":4}}` | `409`: identificação repetida |
| 12 | `PUT /mercadoria/atualizar/1` | `{"quantidade":25}` | `200`, quantidade 25. Valor e demais campos mantidos |
| 13 | `PUT /usuario/associar/4/mercadoria/1` | — | `400`: Ana não é fornecedora |
| 14 | `POST /empresa/cadastro` | `{"razaoSocial":"Filial ltda"}` | `201`, empresa id 2 |
| 15 | `PUT /empresa/associar/2/usuario/4` | — | `200`, Ana agora está na empresa 2. Em `GET /empresa/1`, ela não aparece mais na lista `usuarios` |
| 16 | `DELETE /mercadoria/excluir/1` | — | `409`: a mercadoria faz parte de vendas |
| 17 | `DELETE /venda/excluir/3` | — | `204`. Em seguida, `GET /usuario/4` mostra a Ana sem vendas |
| 18 | `DELETE /veiculo/excluir/2` | — | `204`, o veículo não tem mais vendas |
| 19 | `DELETE /usuario/excluir/4` | — | `204` |
| 20 | `GET /usuario/99` | — | `404` |

### Usando `curl` (opcional)

No **PowerShell**, digite `curl.exe` em vez de `curl`.

```bash
curl http://localhost:8080/venda/1

curl -X POST http://localhost:8080/usuario/cadastro/1 \
     -H "Content-Type: application/json" \
     -d '{"nome":"Ana","perfis":["CLIENTE"]}'

curl -X PUT http://localhost:8080/empresa/associar/1/usuario/4

curl -X DELETE http://localhost:8080/venda/excluir/2
```

---

## 10. Banco de dados (H2 Console)

Com a aplicação rodando:

1. Acesse <http://localhost:8080/h2-console>
2. Preencha:

   | Campo | Valor |
   |---|---|
   | JDBC URL | `jdbc:h2:mem:automanager` |
   | User Name | `sa` |
   | Password | *(vazio)* |

3. Clique em **Connect** e consulte, por exemplo, `SELECT * FROM VENDA;` ou `SELECT * FROM VENDA_MERCADORIAS;`.

Tabelas como `EMPRESA_USUARIOS`, `USUARIO_VEICULOS` e `VENDA_SERVICOS` fazem a ligação entre as entidades. Os links do HATEOAS não são gravados no banco: são montados a cada resposta.

---

## 11. Alterações feitas no código-base

A equipe disponibilizou as entidades do diagrama (repositório [`atviii-autobots-microservico-spring`](https://github.com/gerson-pn/atviii-autobots-microservico-spring)). O enunciado permite modificar esse código, desde que nenhuma funcionalidade seja negligenciada. As alterações foram:

| # | Onde | Situação no código-base | Alteração |
|---|---|---|---|
| 1 | `pom.xml` | Sem `spring-boot-starter-web`: a aplicação não tinha API HTTP | Adicionado |
| 2 | `pom.xml` | Sem `spring-boot-starter-hateoas` | Adicionado |
| 3 | `pom.xml` / `application.properties` | Configurado para **MySQL** em `localhost` (usuário/senha `root`), que precisaria estar instalado | Trocado por **H2 em memória**, como na ATVII: roda sem instalar nada |
| 4 | `pom.xml` | Lombok sem versão fixa (1.18.22), que **não compila no JDK 21** | Fixado o Lombok 1.18.34 |
| 5 | `pom.xml` | `spring-boot-starter-data-jdbc` e `starter-jdbc` junto com o JPA, sem uso | Removidos |
| 6 | Pacote | `entitades` (erro de digitação) | Renomeado para `entidades`, como na ATVII |
| 7 | `Mercadoria` | Campo `fabricao` (erro de digitação, também no diagrama) | Renomeado para `fabricacao` |
| 8 | Entidades | Sem HATEOAS | Todas estendem `RepresentationModel`, com `@EqualsAndHashCode(callSuper = false)` |
| 9 | `Venda`, `Veiculo`, `Usuario` | Relacionamentos nos dois sentidos geravam **loop infinito** no JSON e no `toString` do Lombok | `@JsonIgnoreProperties` nos lados internos (seção 6) e `@ToString(exclude = ...)` |
| 10 | `Venda` | Mercadorias e serviços eram `@OneToMany`, que no JPA cria coluna única: a **mesma peça não podia estar em duas vendas** (por isso o código-base duplicava a roda e o alinhamento) | `@ManyToMany`. A carga de exemplo agora reaproveita a mesma roda e o mesmo alinhamento |
| 11 | `Venda` | Veículo era `@OneToOne`, mas um veículo tem várias vendas | `@ManyToOne` |
| 12 | `Credencial` | Classe abstrata: o JSON de entrada não tinha como saber qual subclasse criar | `@JsonTypeInfo` com o campo `tipo` (`usuarioSenha` / `codigoBarra`) |
| 13 | `Mercadoria`, `Servico`, `Credencial` | Campos `double`, `long` e `boolean` (primitivos): num `PUT` parcial, um campo não enviado viraria `0`/`false` | Trocados por `Double`, `Long` e `Boolean` |
| 14 | Carga de exemplo | `usuario.vendas` nunca era preenchido. A ordem de gravação variava entre execuções, e com ela os ids | As vendas entram no histórico do cliente. Ordem fixa, então os ids são sempre os da seção 7 |
| 15 | — | Só existia o repositório da empresa, sem nenhum controle | Repositório, controle, atualizador e adicionador de links para as 11 entidades, mais as rotas de associação |
| 16 | — | Erros de banco (valor único repetido) respondiam `500` | `TratadorExcecoes` converte em `409` |
| 17 | `CredencialUsuarioSenha` | A senha seria devolvida em toda resposta que mostra credenciais | Campo somente de escrita (`@JsonProperty(access = WRITE_ONLY)`): entra no cadastro, não sai na resposta. A proteção completa fica para a "atualização de segurança" |

### Estrutura do projeto

```
automanager/src/main/java/com/autobots/automanager/
├── AutomanagerApplication.java   → inicialização + dados de exemplo
├── controles/                    → rotas HTTP (um controle por entidade) + TratadorExcecoes
├── entidades/                    → classes do diagrama (estendem RepresentationModel)
├── enumeracoes/                  → PerfilUsuario, TipoDocumento, TipoVeiculo
├── modelo/
│   ├── AdicionadorLink*.java         → links HATEOAS de cada entidade
│   ├── *Atualizador.java             → atualização parcial dos dados
│   └── StringVerificadorNulo.java    → verifica textos nulos ou em branco
└── repositorios/                 → acesso ao banco (Spring Data JPA)
```

---

## 12. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `'java' não é reconhecido...` | Java não instalado ou fora do PATH | Instalar o JDK 17+ |
| `JAVA_HOME not found` | Variável `JAVA_HOME` não configurada | Apontar `JAVA_HOME` para a pasta do JDK |
| `mvnw : O termo 'mvnw' não é reconhecido` (PowerShell) | O PowerShell exige `.\` | `.\mvnw.cmd spring-boot:run` |
| `Port 8080 was already in use` | Outra instância rodando | Encerrar a outra, ou: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` |
| Eclipse: `The method getXxx() is undefined` | Plugin do Lombok não instalado | Seção 2, Opção B, passo 1 |
| `400` ao cadastrar credencial | Faltou o campo `"tipo"` | Enviar `"tipo": "usuarioSenha"` ou `"tipo": "codigoBarra"` |
| `409` ao excluir mercadoria, serviço, veículo ou usuário | Faz parte de uma venda | Excluir antes a(s) venda(s) (seção 8.4) |
| A senha não aparece no JSON da credencial | Ela é somente de escrita | Comportamento esperado. Ela continua gravada e pode ser alterada pelo `PUT` |
| `400` ao cadastrar venda com dados corretos | Cliente, funcionário, mercadoria ou serviço de **outra empresa**, ou veículo de outro cliente | Usar registros da empresa da URL (seção 8.3) |
| Ao parar a aplicação aparece `Database is already closed` | O H2 em memória fecha antes do Hibernate | Inofensivo, pode ignorar |
| Datas com um dia a mais ou horário estranho | Datas exibidas em **UTC** | Comportamento esperado |
