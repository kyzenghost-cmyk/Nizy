# CLONE X — espaços isolados por usuários Android

O CLONE X agora usa o mecanismo multiusuário nativo do Android, não reempacota APKs e não injeta hooks no processo do WhatsApp.

## Como funciona

- O CLONE X é provisionado como **Device Owner** do aparelho.
- O Device Owner cria usuários Android secundários com `DevicePolicyManager.createAndManageUser()`.
- Cada usuário possui armazenamento privado, contas, configurações e sessões independentes.
- O CLONE X é instalado como Profile Owner em cada usuário criado.
- Dentro de cada espaço, `DevicePolicyManager.installExistingPackage()` instala a cópia existente do aplicativo original, preservando pacote, assinatura e código.
- O usuário alterna entre espaços pelo seletor de usuários do Android/Samsung.

## Requisito crítico de provisionamento

Um aplicativo comum não pode se promover a Device Owner em um telefone já configurado. O provisionamento de Device Owner é feito durante o assistente inicial, em aparelho novo ou após redefinição de fábrica. Para desenvolvimento, o Android também oferece o comando ADB:

```bash
adb shell dpm set-device-owner app.clonex.profile/.CloneXAdminReceiver
```

**Atenção: esse procedimento pode exigir redefinição de fábrica e apaga os dados do aparelho. Faça backup antes. Não execute o comando em seu telefone principal sem compreender o impacto.**

O APK atual não executa redefinição, não ativa Device Owner sozinho e não contorna as proteções do Android.

## Limites reais

- O Android/fabricante decide se múltiplos usuários estão habilitados e quantos podem existir.
- O CLONE X limita a interface a 10 espaços, mas Samsung/Android pode impor limite inferior.
- O método depende de o firmware permitir multiusuário e de o app original continuar disponível para instalação nos outros usuários.
- Notificações, Play Services, backup, mídia e comportamento do WhatsApp precisam ser testados em aparelho físico.
- Não há confirmação de compatibilidade funcional com o Samsung SM-A750G/Android 10 até executar o provisionamento e testar nesse modelo.

## Build

GitHub Actions compila `clone-x-dpc` e publica o artefato `CLONE-X-APK`. Build bem-sucedido significa apenas que o APK foi compilado e verificado; não significa que o firmware do aparelho aceita Device Owner ou 10 usuários.

## API Android usada

- `DevicePolicyManager.createAndManageUser` (API 24+)
- `DevicePolicyManager.installExistingPackage` (API 28+)
- `DevicePolicyManager.switchUser` (API 28+)

Documentação oficial: https://developer.android.com/reference/android/app/admin/DevicePolicyManager
