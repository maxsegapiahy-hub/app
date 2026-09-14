# Exportação do Max Seg & Max Saúde — Apiahy

## Conteúdo

Este pacote contém o aplicativo Expo/React Native, o servidor tRPC, as regras compartilhadas, os testes automatizados e a configuração PWA para a versão web.

## Requisitos

Use Node.js 22 ou superior e pnpm 9 ou superior. Depois de extrair o pacote, execute `pnpm install`.

## Desenvolvimento

Execute `pnpm dev` para iniciar o servidor tRPC e o Metro Web. Para validar o projeto, execute `pnpm test`, `pnpm check` e `pnpm lint`.

## PWA

A versão web está configurada com manifesto instalável em `public/manifest.json`, service worker em `public/sw.js`, metadados PWA no `app.config.ts` e registro do service worker em `app/_layout.tsx`. Gere a versão web com `npx expo export --platform web`. Publique o diretório `dist` em um servidor HTTPS para habilitar a instalação e o cache offline básico.

## Funcionalidades recentes

O perfil permite cadastrar até três contatos de emergência, definir um único contato principal e configurar o nome e telefone da Central Max. O perfil e os contatos são persistidos localmente e sincronizados pelo procedimento `profile.sync`. O alerta SOS encaminha os contatos cadastrados e exibe a quantidade preparada para aviso.

## Observação sobre produção

A rota de sincronização e o envio de avisos são simulados para demonstração. Antes de produção, substitua o procedimento por armazenamento autenticado e por um provedor real de notificações push ou SMS, mantendo os dados protegidos e obtendo consentimento dos contatos.
