# Exportação do Max Seg & Max Saúde — Apiahy

## Conteúdo

Este pacote contém o aplicativo Expo/React Native, o servidor tRPC, as regras compartilhadas, os testes automatizados e a configuração PWA para a versão web.

## Requisitos

Use Node.js 22 ou superior e pnpm 9 ou superior. Depois de extrair o pacote, execute `pnpm install`.

## Desenvolvimento

Execute `pnpm dev` para iniciar o servidor tRPC e o Metro Web. Para validar o projeto, execute `pnpm test`, `pnpm check` e `pnpm lint`.

## Backend autenticado e push

O perfil agora é persistido por usuário autenticado nas tabelas `user_profiles` e `push_tokens`. As rotas de perfil, registro de token, SOS e cancelamento exigem sessão OAuth. O SOS envia notificações reais pelo Expo Push Service aos dispositivos autenticados do titular. Configure `EXPO_PROJECT_ID`, credenciais APNs/FCM no EAS e use uma development build ou release build física; consulte `PUSH_NOTIFICATIONS.md` para o procedimento completo.

## PWA

A versão web está configurada com manifesto instalável em `public/manifest.json`, service worker em `public/sw.js`, metadados PWA no `app.config.ts` e registro do service worker em `app/_layout.tsx`. Gere a versão web com `npx expo export --platform web`. Publique o diretório `dist` em um servidor HTTPS para habilitar a instalação e o cache offline básico.

## Funcionalidades recentes

O perfil permite cadastrar até três contatos de emergência, definir um único contato principal e configurar o nome e telefone da Central Max. O perfil e os contatos são persistidos localmente e sincronizados pelo procedimento `profile.sync`. O alerta SOS encaminha os contatos cadastrados e exibe a quantidade preparada para aviso.

## Observação sobre produção

O envio SMS não está ativado por padrão porque exige provedor, credenciais, número remetente e consentimento. Ele pode ser conectado no backend como um adapter do evento `sos.send`, sem expor chaves no aplicativo.
