# IMDTravel — Unidade 2, Parte 1

Aplicação de compra de passagens construída com Java 25 e Spring Boot. O IMDTravel consulta o AirlinesHub, obtém a taxa do Exchange, vende o assento, registra os pontos no Fidelity e persiste a venda em JSON.

## Pré-requisitos

- Docker Desktop com o engine Linux ativo e Docker Compose v2.
- Acesso ao Docker Hub para obter as imagens `gibeonufrn/imdtravel-{airlineshub,exchange,fidelity}:baseline`.
- Para executar os testes Maven fora do Docker, JDK 25.
- Para executar `testes.sh`, Bash, `curl`, `jq` e Docker Compose.

## Iniciar o sistema

Na raiz do repositório:

```sh
docker compose up -d --wait --build
```

O Compose cria os quatro serviços em uma rede interna. Somente a porta do IMDTravel é publicada no host (8080 por padrão). As vendas ficam no volume nomeado `sales-data` e sobrevivem à recriação dos containers.

Verificar saúde:

```sh
curl -i http://localhost:8080/health
```

Parar os serviços sem remover os dados:

```sh
docker compose down
```

Não use `docker compose down -v` se precisar preservar o volume de vendas.

## Configuração

As opções podem ser definidas no ambiente ou em um arquivo `.env` na raiz:

| Variável | Padrão | Uso |
|---|---|---|
| `IMDTRAVEL_HOST_PORT` | `8080` | Porta do IMDTravel no host |
| `IMAGE_TAG` | `baseline` | Tag das imagens externas |
| `CATALOG_FLIGHTS` | `IMD100,IMD200,IMD300` | Voos do AirlinesHub |
| `CATALOG_BASE_PRICES` | `350.50,420.50,510.50` | Preços-base em USD |
| `CATALOG_PRICE_STEP_PER_DAY` | `12.25` | Variação diária de preço |
| `CATALOG_START_DATE` | `2026-12-01` | Primeiro dia do catálogo |
| `CATALOG_DAYS` | `7` | Dias por voo |
| `CATALOG_SEATS` | `20` | Assentos por voo/dia |
| `SELL_MIN_MS` / `SELL_MAX_MS` | `300` / `900` | Faixa de latência de venda |
| `EXCHANGE_INITIAL_RATE` | `5.50` | Taxa inicial |
| `EXCHANGE_TICK_S` | `10` | Intervalo de atualização da taxa |
| `EXCHANGE_MAX_STEP_PCT` | `0.3` | Variação máxima por atualização |
| `EXCHANGE_MIN` / `EXCHANGE_MAX` | `5.00` / `6.00` | Limites da taxa |

O IMDTravel também aceita `AIRLINESHUB_URL`, `EXCHANGE_URL`, `FIDELITY_URL` e `SALES_DIR`; no Compose, os três primeiros apontam aos nomes DNS internos e `SALES_DIR` aponta para `/data`.

## API

### Comprar passagem

`POST /buyTicket`, JSON:

```json
{
  "flight": "IMD100",
  "day": "2026-12-01",
  "user": "ana"
}
```

O fluxo é síncrono e sequencial: AirlinesHub `GET /flight`, Exchange `GET /convert`, AirlinesHub `POST /sell` e Fidelity `POST /bonus`. O sucesso retorna HTTP 201 com `transactionId`, `flight`, `day`, `user`, `valueUSD`, `valueBRL`, `rate`, `rateAsOf` e `bonus`. Voo inexistente retorna 404, assento esgotado retorna 409, e falhas internas/de dependências retornam 500.

O cliente pode enviar `X-Correlation-Id`; se não enviar, o IMDTravel gera um UUID. O identificador é retornado no cabeçalho e propagado às chamadas externas.

### Consultar vendas

`GET /sales` retorna os registros persistidos, incluindo correlation ID, transaction ID, voo, dia, usuário, valores e instante UTC.

## Testes

Testes unitários:

```sh
cd imdtravel
./mvnw test
```

No PowerShell, use `.\mvnw.cmd test`. Os testes de integração contra serviços reais são separados e podem modificar estoque/pontos:

```sh
./testes.sh
```

O script testa o sistema em execução. Não use a opção `--reiniciar` se precisar preservar os dados: ela derruba os containers e remove volumes. Consulte [tests/README.md](./tests/README.md) para os cenários e registro de evidências.

## Serviços externos

O repositório contém a aplicação IMDTravel; AirlinesHub, Exchange e Fidelity são consumidos como imagens Docker externas. O comportamento desses serviços depende das imagens e tags disponíveis no Docker Hub.
