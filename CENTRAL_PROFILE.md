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
