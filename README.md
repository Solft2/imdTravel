# IMDTravel

O IMDTravel é uma aplicação de reserva de passagens que integra o catálogo de
voos, o câmbio e o programa de fidelidade. Ela permite consultar um voo,
comprar uma passagem e consultar as vendas realizadas.

## Linguagem e framework

- **Linguagem:** Java 25.
- **Framework:** Spring Boot 4.1.2, com Spring WebMVC e Spring Actuator.
- **Gerenciador de dependências e build:** Apache Maven (por meio do Maven Wrapper).
- **Conteinerização:** Docker e Docker Compose.
- **Formato das APIs:** JSON sobre HTTP.

O projeto utiliza `RestClient` para comunicar-se com os serviços externos
`airlineshub`, `exchange` e `fidelity`. Um filtro gera ou propaga o
`X-Correlation-Id` entre as requisições e as vendas.

## Equipe

Trabalho realizado em colaboração por:

- Arthur Eric Torres Galvão
- Samuel Victor
- Jorge William

## Catálogo de voos

O catálogo utilizado pelo serviço `airlineshub` possui, por padrão, os voos:

| Voo | Preço-base em USD |
| --- | ---: |
| IMD100 | 350,50 |
| IMD200 | 420,50 |
| IMD300 | 510,50 |

Os demais parâmetros padrão do catálogo são:

- **Data inicial:** 2026-12-01.
- **Horizonte:** 7 dias a partir da data inicial.
- **Assentos por voo e dia:** 20.
- **Acréscimo de preço por dia:** USD 12,25.

Assim, o preço de cada voo varia conforme o dia da viagem. Os valores podem
ser alterados pelas variáveis de ambiente `CATALOG_FLIGHTS`,
`CATALOG_BASE_PRICES`, `CATALOG_PRICE_STEP_PER_DAY`, `CATALOG_START_DATE`,
`CATALOG_DAYS` e `CATALOG_SEATS` no arquivo `docker-compose.hub.yml`.

## Testes realizados

### Testes unitários

A suíte Maven cobre a inicialização da aplicação, a configuração do cliente
HTTP, a propagação do ID de correlação e a comunicação com os serviços externos.
Os casos incluem:

- carregamento do contexto da aplicação;
- criação e limpeza do contexto de correlação;
- reutilização de um `X-Correlation-Id` recebido e geração de UUID;
- propagação do ID de correlação nas chamadas HTTP;
- consulta e venda no `airlineshub`, incluindo conflito por falta de assentos;
- consulta da cotação no `exchange`.

Para executar os testes unitários:

```bash
cd imdtravel
./mvnw test
```

### Testes funcionais

O script `testes.sh` verifica o sistema executando requisições reais ao
`GET /health`, `POST /buyTicket` e `GET /sales`. Os testes conferem:

- saúde dos quatro serviços;
- compra bem-sucedida e redução correta do estoque;
- registro da venda e conversão entre USD e BRL;
- crédito de pontos no programa de fidelidade;
- geração e propagação do ID de correlação;
- estabilidade do preço e da cotação;
- respostas `400`, `404` e `409` para pedidos inválidos, inexistentes ou sem
  assentos;
- esgotamento concorrente sem estoque negativo;
- falha de fluxo opcional com o serviço de fidelidade temporariamente parado.

Os testes funcionais não dependem do sistema estar vazio: selecionam pares
voo/dia disponíveis e comparam os estados antes e depois de cada operação.

Para subir o ambiente e executar os testes:

```bash
docker compose -f docker-compose.hub.yml up -d --wait --build
./testes.sh
```

Para reiniciar o ambiente apagando os dados persistidos antes do teste:

```bash
./testes.sh --reiniciar
```

O script pode incluir o cenário de falha do serviço de fidelidade com:

```bash
./testes.sh --com-falhas
```
