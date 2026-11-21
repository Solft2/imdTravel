# Testes e evidências

## Testes automatizados do IMDTravel

Os testes JUnit em `imdtravel/src/test` cobrem os clientes AirlinesHub, Exchange e Fidelity, a sequência das chamadas de compra, o cálculo dos valores e bônus, endpoints de compra/vendas/health, mapeamento de voo inexistente/estoque esgotado/falha externa, propagação de correlation ID e persistência entre instâncias do repositório.

Execute de `imdtravel`:

```sh
./mvnw test
```

Os relatórios Surefire são gerados em `imdtravel/target/surefire-reports/`. Essa saída local é ignorada pelo Git e não deve ser apresentada como evidência versionada.

## Testes integrados

Com os quatro serviços iniciados por `docker compose up -d --wait --build`, execute na raiz:

```sh
./testes.sh
```

O script cobre compra bem-sucedida, correlation ID, cálculo de valores, validação, voo inexistente, esgotamento concorrente e consulta de pontos. `./testes.sh --com-falhas` inclui um cenário que para o Fidelity e altera estado. `./testes.sh --reiniciar` remove volumes; evite essa opção quando houver dados a preservar.

## Registro de execução

### Testes unitários do IMDTravel

- Data: 2026-11-21.
- Branch base: `feature/dockerizar-imdtravel` (`15e8227` antes das alterações desta tarefa).
- Comando: `mvnw.cmd "-Dmaven.compiler.release=24" clean test`.
- Ambiente: Windows, JDK 24.0.2.
- Resultado: 23 testes executados, 0 falhas, 0 erros, 0 ignorados.
- Limitação: o projeto exige Java 25; esse resultado confirma o comportamento com release 24, não substitui uma execução no JDK 25.
- Relatórios: `imdtravel/target/surefire-reports/` (artefatos locais ignorados pelo Git).

### Smoke test local

- Data: 2026-11-21.
- Execução: aplicação iniciada temporariamente na porta 18080, fora do Docker.
- Resultado: `/health` retornou HTTP 200 com `status=ok` e `uptimeS`; resposta trouxe `X-Correlation-Id`; `/sales` retornou `[]`.
- Limitação: não exercita o fluxo de compra nem as integrações externas.

### Testes integrados

Não executados nesta validação: o Docker Engine não estava ativo. Ainda precisam ser executados com os quatro serviços em funcionamento e ter seus resultados registrados aqui. Não registre segredos nem dados pessoais.
