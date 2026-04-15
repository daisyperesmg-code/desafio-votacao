# Cooperativa Votação — API REST

API REST para gerenciamento de sessões de votação em assembleias cooperativas.
Cada associado possui um voto por pauta, e as decisões são contabilizadas ao encerramento da sessão.

---

## Tecnologias utilizadas
Java 17
Spring Boot 3.2
Spring Data JPA + Hibernate
PostgreSQL 
Lombok 
springdoc-openapi (Swagger UI)
Docker + Docker Compose
Maven

---

## Arquitetura e decisões de projeto

### Estrutura de pacotes

com.example.votacao
├── client/        # Facade de integração externa (CPF)
├── config/        # Configuração global (OpenAPI, tratamento de exceções)
├── controller/    # Camada HTTP — recebe e responde requisições
├── dto/           # Objetos de transferência (records imutáveis)
├── entity/        # Entidades JPA persistidas
├── enums/         # Tipos enumerados (TipoVoto, StatusVoto)
├── mapper/        # Conversão entre entidade e DTO
├── repository/    # Interfaces Spring Data JPA
└── service/       # Regras de negócio

A separação em camadas (controller → service → repository) mantém as responsabilidades claras e facilita testes unitários com mock por camada.

### Controle de unicidade no banco

A restrição de voto único por associado é garantida  por uma `UniqueConstraint` na tabela `Voto`. Isso protege contra race conditions em cenários de alta concorrência: mesmo que dois votos do mesmo associado passem pela verificação ao mesmo tempo, o banco rejeita o segundo com `DataIntegrityViolationException`.
A verificação prévia no service (`existsByPautaIdAndAssociadoId` antes de salvar)  é usada apenas para reduzir tentativas desnecessárias de gravação.

### Controle de concorrência e consistência

A consistência em cenários de concorrência é garantida por constraint única no banco de dados. A aplicação trata violações de integridade (DataIntegrityViolationException) como regra de negócio.

### Índice de performance

@Index(name = "idx_voto_pauta_voto", columnList = "pauta_id, voto")

A contagem de votos por pauta e tipo (`countByPautaIdAndVoto`) é a query mais executada no endpoint de resultado. O índice composto em `(pauta_id, voto)` permitindo ao otimizador do PostgreSQL utilizar index scan em vez de full table scan — relevante no cenário de centenas de milhares de votos.

### Tratamento de exceções centralizado

O `GlobalExceptionHandler` captura três categorias:
- `BusinessException` → 422 (regra de negócio violada: sessão encerrada, voto duplicado, etc.)
- `ResourceNotFoundException` → 404 (pauta ou sessão não encontrada, CPF inválido)
- `MethodArgumentNotValidException` → 400 (validação de campos com `@Valid`)

Isso mantém os controllers e o service sem `try/catch` espalhados, e garante respostas consistentes para o cliente mobile.

### Client do CPF (Bônus 1)

O `CpfClient` é uma interface. O `CpfClientFake` é a implementação injetada via `@Component`. Isso permite trocar por uma implementação real no futuro sem alterar nenhuma linha de código do service — basta criar uma nova implementação da interface e configurar qual bean usar por `@Profile` ou `@ConditionalOnProperty`.

A validação externa de CPF é realizada após validações internas para reduzir chamadas desnecessárias e minimizar dependência de serviços externos em fluxos que já seriam rejeitados por regras de negócio locais.

---

## Pré-requisitos

- Docker, Docker Compose, Maven (instalados)
- Portas `8080` e `5432` disponíveis na máquina

---

## Executando com Docker Compose (recomendado)

```bash
# 1. Clone o repositório
git clone https://github.com/daisyperesmg-code/desafio-votacao.git
cd desafio-votacao
mvn clean package

# 2. Suba os containers (banco + aplicação)
docker compose up --build
```

A aplicação estará disponível em `http://localhost:8080`.


## Executando localmente (sem Docker)

Ajuste do application.properties

Certifique-se de que a URL do banco aponta para `localhost`:

spring.datasource.url=jdbc:postgresql://localhost:5432/votacao


## Documentação da API (Swagger UI)

http://localhost:8080/swagger-ui/index.html


## Logs

Os logs da aplicação são gravados em `./logs/app.log` (mapeado via volume no Docker Compose) com rotação automática a cada 10 MB, retendo os últimos 7 arquivos.


## Testes

Os testes unitários cobrem os principais fluxos do `VotacaoService`:
