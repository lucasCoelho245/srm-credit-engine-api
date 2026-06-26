# SRM Credit Engine — API

> Motor de precificação e liquidação de recebíveis para fundos FIDC.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Tests](https://img.shields.io/badge/testes-19%20passando-brightgreen)

## Visão geral

O **SRM Credit Engine** é uma API REST que permite a operadores de FIDC precificar e liquidar recebíveis com suporte a múltiplas moedas.

**O que o sistema faz:**

1. Uma empresa cedente possui uma duplicata de R$ 10.000 que vence em 3 meses
2. O operador simula o deságio — o fundo calcula quanto paga hoje pelo título
3. Se aprovado, o operador liquida: a transação é persistida com rastreabilidade completa
4. O extrato consolida todas as operações com filtros por data, cedente e moeda

**Fórmula de precificação:**

```
VP = VF / (1 + taxaBase + spread) ^ prazo
Deságio = VF - VP
```

Exemplo canônico: VF = R$ 10.000 | taxaBase = 1% | spread = 1,5% | prazo = 3 meses
→ **VP = R$ 9.285,9941 | Deságio = R$ 714,0059**

---

## Stack

| Camada | Tecnologia | Motivo da escolha |
|---|---|---|
| Linguagem | Java 21 | Tipagem forte, Records, padrão do mercado financeiro |
| Framework | Spring Boot 3 | Ecossistema maduro, ACID nativo, Swagger integrado |
| Persistência | Spring Data JPA + Hibernate | CRUD sem SQL repetitivo, suporte a JPQL tipado |
| Banco dev | H2 in-memory | Zero configuração, inicia em milissegundos |
| Banco prod | PostgreSQL 16 | Banco relacional robusto, compatível com o mercado |
| Containers | Docker + Compose | Ambiente reproduzível, sem "na minha máquina funciona" |
| Docs | Swagger / OpenAPI 3 | Documentação interativa dos endpoints |
| Testes | JUnit 5 + Mockito + AssertJ | 19 casos, 100% passando |

---

## Rodando localmente (H2)

**Pré-requisito:** Java 21

```bash
./mvnw spring-boot:run
```

O perfil `dev` sobe automaticamente com **dados de seed** (moedas BRL/USD, taxa de câmbio e tipos de produto) — nenhuma configuração adicional é necessária.

| Serviço | URL |
|---|---|
| API base | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

Credenciais H2: JDBC URL `jdbc:h2:mem:srm_dev` · usuário `sa` · senha em branco

---

## Rodando com Docker (PostgreSQL)

```bash
docker compose up --build
```

Sobe dois containers em ordem:

1. **`srm_postgres`** — PostgreSQL 16, com healthcheck antes de liberar o backend
2. **`srm_backend`** — API Spring Boot com perfil `prod`, apontando para o PostgreSQL

API disponível em `http://localhost:8080` após ambos estarem saudáveis.

---

## Endpoints

### Moedas

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/v1/currencies` | Lista moedas disponíveis (BRL, USD) |

### Taxas de câmbio

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/v1/exchange-rates` | Lista taxas cadastradas |
| `POST` | `/api/v1/exchange-rates` | Cadastra ou atualiza taxa de câmbio |

### Tipos de produto

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/v1/product-types` | Lista tipos de recebível (Duplicata, Cheque) |

### Recebíveis

| Método | URL | Descrição |
|---|---|---|
| `POST` | `/api/v1/receivables/simulate` | Calcula VP e deságio **sem persistir** |
| `POST` | `/api/v1/receivables/liquidate` | Liquida e **persiste** a transação |

**Exemplo — simulate (request):**
```json
{
  "cedente": "Petrobras S.A.",
  "faceValue": 10000,
  "baseRate": 0.01,
  "termMonths": 3,
  "productTypeId": "<uuid>",
  "titleCurrencyId": "<uuid-brl>",
  "paymentCurrencyId": "<uuid-brl>"
}
```

**Exemplo — simulate (response):**
```json
{
  "presentValue": 9285.9941,
  "discount": 714.0059,
  "exchangeRateUsed": null
}
```

Para operação cross-currency (título em USD, pagamento em BRL), `exchangeRateUsed` retorna a taxa utilizada.

### Transações e relatórios

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/v1/transactions` | Listagem paginada de transações |
| `GET` | `/api/v1/reports/extract` | Extrato filtrado por data, cedente e moeda |

Parâmetros do extrato: `startDate`, `endDate`, `cedente`, `paymentCurrencyCode`, `page`, `size`

---

## Arquitetura de pacotes

O projeto usa **feature-based layered architecture** — cada domínio agrupa suas próprias camadas, tornando o código de cada contexto navegável em um único lugar.

```
br.com.srm
├── common/
│   ├── config/          → CORS, Swagger, WebMvc
│   ├── exception/       → BusinessException, ResourceNotFoundException, GlobalExceptionHandler
│   └── interceptor/     → LoggingInterceptor (logging centralizado de todas as requisições)
├── currency/
│   ├── controller/      → CurrencyController, ExchangeRateController
│   ├── domain/          → Currency, ExchangeRate
│   ├── dto/             → CurrencyResponse, ExchangeRateRequest/Response
│   ├── repository/      → CurrencyRepository, ExchangeRateRepository
│   └── service/         → CurrencyService
├── receivable/
│   ├── controller/      → ReceivableController
│   ├── domain/          → Transaction, ProductType
│   ├── dto/             → SimulateRequest/Response, LiquidateRequest/Response, ProductTypeResponse
│   ├── pricing/         → PricingStrategy (interface), DuplicataStrategy, ChequeStrategy,
│   │                      PricingEngine, PricingStrategyFactory
│   ├── repository/      → TransactionRepository, ProductTypeRepository
│   └── service/         → ReceivableService
└── reports/
    ├── controller/      → ReportController
    ├── dto/             → TransactionPageResponse
    ├── repository/      → ReportRepository (JPQL com expressão construtora)
    └── service/         → ReportService
```

---

## Decisões de design

### Strategy Pattern — motor de precificação

Cada tipo de recebível tem um spread diferente. Em vez de `if/else` crescente, cada produto implementa a interface `PricingStrategy`. Adicionar um novo tipo requer apenas uma nova classe `@Component` — sem modificar `PricingEngine` nem `PricingStrategyFactory` (Open/Closed Principle).

```
PricingStrategy (interface)
├── DuplicataStrategy  → spread = 1,50% a.m.
└── ChequeStrategy     → spread = 2,50% a.m.
```

O Spring injeta automaticamente todas as implementações na `PricingStrategyFactory` via `List<PricingStrategy>`.

### JPQL com expressão construtora

O extrato e a listagem usam `SELECT new dto.Class(...)` em vez de SQL nativo + `Object[]`. O Hibernate projeta o resultado diretamente no DTO tipado, eliminando ~50 linhas de conversores manuais e funcionando igual com H2 e PostgreSQL.

### BigDecimal com HALF_EVEN

Toda operação financeira usa `BigDecimal` com escala de 4 casas decimais e arredondamento `HALF_EVEN` (banker's rounding). `double` é proibido em cálculos monetários — seu erro de ponto flutuante é inaceitável em sistemas financeiros.

### Transações ACID

`liquidate` usa `@Transactional(rollbackFor = Exception.class)` — se qualquer etapa falhar, nenhuma persistência acontece. `simulate` usa `@Transactional(readOnly = true)` para otimizar a leitura sem locks de escrita.

---

## Testes

```bash
./mvnw test
```

| Classe | Casos | Cobertura |
|---|---|---|
| `PricingEngineTest` | 9 | Fórmula VP, arredondamento HALF_EVEN, validações de entrada |
| `PricingStrategyFactoryTest` | 4 | Resolução da strategy correta por nome de produto |
| `ReceivableServiceTest` | 4 | Simulação BRL, liquidação cross-currency, erros esperados |
| `ReportServiceTest` | 2 | Delegação ao repositório, tipagem correta da página |
| **Total** | **19** | **100% passando** |

Os testes de service usam Mockito para isolar a lógica de negócio do banco de dados, tornando-os rápidos e determinísticos.

---

## Frontend

O frontend Angular está no repositório separado:

👉 **[srm-credit-engine-web](https://github.com/lucasCoelho245/srm-credit-engine-web)**

Demo pública (Azure Static Web Apps):
**https://mango-hill-003c3da10.7.azurestaticapps.net/operator**
