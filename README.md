# SRM Credit Engine API

API Spring Boot responsável por precificar, simular e liquidar recebíveis.

Repositório da API do projeto SRM Credit Engine. O frontend fica separado em
`srm-credit-engine-web`.

## Requisitos

- Java 21
- Maven Wrapper incluso no projeto

## Rodando Localmente

```bash
./mvnw spring-boot:run
```

Por padrão a aplicação sobe com perfil `dev`, usando H2 em memória e dados de seed.

URLs:

| Serviço | URL |
|---|---|
| API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

Dados do H2:

- JDBC URL: `jdbc:h2:mem:srm_dev`
- Usuário: `sa`
- Senha: em branco

## Docker

Na raiz do projeto:

```bash
docker compose up --build
```

Esse fluxo sobe PostgreSQL e API. Depois disso, a API fica disponível em
`http://localhost:8080`.

## Endpoints

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/v1/currencies` | Lista moedas |
| `GET` | `/api/v1/exchange-rates` | Lista taxas de câmbio |
| `POST` | `/api/v1/exchange-rates` | Cadastra ou atualiza taxa de câmbio |
| `GET` | `/api/v1/product-types` | Lista tipos de recebível |
| `POST` | `/api/v1/receivables/simulate` | Simula deságio sem persistir |
| `POST` | `/api/v1/receivables/liquidate` | Liquida e persiste o recebível |
| `GET` | `/api/v1/transactions` | Lista transações paginadas |
| `GET` | `/api/v1/reports/extract` | Retorna extrato filtrado |

## Fórmula

```text
VP = VF / (1 + taxaBase + spread)^prazo
Deságio = VF - VP
```

Spreads:

- Duplicata Mercantil: `1.5% a.m.`
- Cheque Pré-datado: `2.5% a.m.`

## Testes

```bash
./mvnw test
```

A suíte cobre cálculo financeiro, seleção de estratégia, liquidação, câmbio e extrato.
