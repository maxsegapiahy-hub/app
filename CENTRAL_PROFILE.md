# Perfil operacional da Central Max Apiahy

A Central Max Apiahy foi preparada como a referência operacional compartilhada entre o aplicativo e o backend.

| Campo | Configuração |
|---|---|
| Identificador | `central-max-apiahy` |
| Nome | Central Max Apiahy |
| Região | Apiaí · SP |
| Telefone | 153 |
| Disponibilidade | 24 horas, todos os dias |
| Serviço | Proteção, saúde e pronta resposta |
| Status inicial | Online |
| Meta de resposta | Pronta resposta estimada em até 3 minutos |

Os canais previstos são SOS com localização GPS, telefone 153 e acompanhamento do protocolo no aplicativo. O endpoint público `central.profile` expõe esse perfil ao cliente; cada resposta autenticada de SOS também inclui a configuração da Central usada no atendimento.

O telefone e o nome continuam editáveis no cadastro local do usuário para permitir ajustes operacionais. A fonte padrão compartilhada permanece em `shared/max-seg.ts`, evitando divergência entre interface, backend e testes.

## Operação administrativa

Operadores com papel `admin` acessam `/admin`. A tela permite editar identidade, canais, disponibilidade, meta de resposta e status operacional da Central. O painel lista protocolos SOS ativos e atualiza os dados automaticamente a cada cinco segundos, permitindo avançar o atendimento entre recebido, despachando, a caminho, no local, cancelado e encerrado.

## Prioridade SOS

O app solicita o tipo de emergência antes de capturar o GPS. Segurança e incêndio recebem prioridade **crítica**; saúde e acidente recebem prioridade **alta**; outras ocorrências recebem prioridade **média**. A mesma regra é aplicada no backend para evitar que uma alteração de interface modifique a classificação operacional.
