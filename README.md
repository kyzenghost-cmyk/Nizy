# WA-GUARD

Camada de monitoramento e proteção para WhatsApp Baileys controlada pelo Telegram.

## Requisitos
- Node.js 20+
- Conta/bot Telegram e token em `BOT_TOKEN`

## Instalação
```bash
npm install
cp .env.example .env
nano .env
npm start
```

O estado de autenticação é persistido em `auth/whatsapp`. **Não apague essa pasta** se já houver uma sessão pareada. O bot não baixa anexos, não aplica limite de mensagens e não bloqueia desconhecidos automaticamente. Alertas são enviados ao Telegram; confiança/bloqueio são ações manuais.

## Comandos Telegram
- `/status` estado
- `/pause` pausa ações automáticas (monitoramento continua)
- `/resume` retoma
- `/trust +5511999999999` confiar número
- `/block +5511999999999` bloquear número
- `/incidents` incidentes recentes

Configure `BOT_TOKEN` e `TELEGRAM_ADMIN_ID` no `.env`.