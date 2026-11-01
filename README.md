# WA-GUARD

Monitoramento de mensagens recebidas no WhatsApp via Baileys, com alertas e comandos de administração pelo Telegram.

## O que está implementado
- Sessão WhatsApp persistida em `auth/whatsapp`.
- Alertas de remetentes que não estão na lista confiável.
- Sinais heurísticos de pedido de senha/código, pressão associada a Pix, iscas de prêmio, URLs HTTP, encurtadores, IPs literais e domínios punycode.
- Detecção por metadados de extensões de anexos potencialmente executáveis; **nenhum anexo é baixado ou aberto**.
- Lista manual de contatos confiáveis e bloqueados.
- Comandos Telegram: `/status`, `/pause`, `/resume`, `/incidents`, `/trust +55...`, `/block +55...`.
- Persistência local de listas, estado e incidentes em `data/`.

## Limites importantes
Este projeto é uma camada heurística de monitoramento, não um antivírus nem uma garantia de impedir golpes, invasões ou banimentos. Alertas podem conter falsos positivos e falsos negativos. Mensagens de grupos são ignoradas. Pix isolado não é tratado como golpe. O bloqueio é manual; não há bloqueio automático de todo desconhecido. Recursos dependentes de suporte específico do WhatsApp/Baileys (privacidade de grupos e rejeição de chamadas) não são prometidos nesta versão.

## Requisitos e instalação
- Node.js 20 ou superior
- Um bot Telegram criado pelo usuário e seu token
- Acesso autorizado à conta WhatsApp que será pareada

```bash
npm install
cp .env.example .env
nano .env
npm test
npm run check
npm start
```

Configure no arquivo `.env`:
```dotenv
BOT_TOKEN=token_do_seu_bot_telegram
TELEGRAM_ADMIN_ID=seu_id_numerico_do_telegram
BOT_NAME=WA-GUARD
```

Use o chat privado do administrador autorizado. Não publique o `.env`, a pasta `auth/whatsapp` nem os arquivos de sessão.

## Sessão e operação
A autenticação é persistida em `auth/whatsapp`. **Nunca apague essa pasta para “corrigir” uma falha** sem entender o estado da sessão. Se o WhatsApp encerrar a sessão, será necessário novo pareamento oficial. Rode sob PM2 em produção e configure reinício automático.

## Testes
`npm test` testa regras determinísticas do analisador. `npm run check` verifica sintaxe. Esses testes não substituem um teste real de pareamento, recebimento de mensagens e entrega de alertas, que exige credenciais e uma conta WhatsApp/Telegram controladas pelo operador.
