# Autenticação e notificações push

O perfil agora é salvo por usuário autenticado nas tabelas `user_profiles` e `push_tokens`. As rotas `profile.get`, `profile.sync`, `push.register`, `sos.send` e `sos.cancel` usam `protectedProcedure`; chamadas sem sessão recebem `UNAUTHORIZED`.

## Configuração do push

1. Crie ou selecione um projeto no Expo Application Services e copie o `projectId`.
2. Defina `EXPO_PROJECT_ID` no ambiente de build, conforme `.env.example`.
3. Configure as credenciais APNs e FCM no EAS para o bundle iOS e o package Android.
4. Gere uma development build ou release build; push remoto não funciona no Expo Go Android a partir do SDK 53.
5. Após login em um dispositivo físico, o app solicita permissão, obtém o Expo Push Token e o registra em `push_tokens`.
6. Ao enviar o SOS, o backend chama o Expo Push Service (`https://exp.host/--/api/v2/push/send`) e retorna `pushSent` e `pushFailed` no protocolo.

O push atualmente entrega o alerta aos dispositivos autenticados do titular. Para que contatos de emergência recebam push, eles precisam ter uma conta no aplicativo e registrar o próprio dispositivo; o próximo passo pode vincular esses dispositivos ao consentimento de cada contato.

## SMS

SMS não foi ativado por padrão porque requer um provedor externo, credenciais, número remetente e regras de consentimento. A integração pode ser adicionada mantendo o mesmo evento `sos.send`, usando um adapter server-side (por exemplo, um provedor de SMS escolhido pelo proprietário) sem expor chaves no aplicativo.
