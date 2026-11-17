# CLONE X — espaços isolados por usuários Android

O CLONE X usa o mecanismo multiusuário nativo do Android. Não reempacota APKs nem injeta hooks no processo do WhatsApp.

## Como funciona

- O CLONE X precisa ser provisionado como **Device Owner**.
- O Device Owner cria usuários Android secundários com `DevicePolicyManager.createAndManageUser()`.
- Cada usuário possui armazenamento privado, contas, configurações e sessões independentes.
- Dentro de cada espaço, `DevicePolicyManager.installExistingPackage()` disponibiliza a instalação do aplicativo original, preservando pacote, assinatura e código.
- O usuário alterna entre espaços pelo seletor de usuários do Android/Samsung.

## Requisito crítico de provisionamento

Um aplicativo comum não pode se promover a Device Owner em um telefone já configurado. O provisionamento é feito durante a configuração inicial ou por ADB em aparelho preparado para testes:

```bash
adb shell dpm set-device-owner app.clonex.profile.v2/app.clonex.profile.CloneXAdminReceiver
```

**Atenção: preparar o aparelho pode exigir redefinição de fábrica e apagar os dados. Faça backup antes. Não execute no telefone principal sem compreender o impacto.**

O APK não ativa Device Owner sozinho nem contorna as proteções do Android.

## Limites reais

- O Android/fabricante decide se múltiplos usuários estão habilitados e quantos podem existir.
- A interface limita a 10 espaços, mas Samsung/Android pode impor limite menor.
- O método depende de o firmware permitir multiusuário.
- Notificações, Play Services, mídia e comportamento do WhatsApp precisam de teste físico.
- Não há confirmação de compatibilidade funcional com o Samsung SM-A750G/Android 10 até testar nesse modelo.

## Build

GitHub Actions publica o artefato `CLONE-X-APK-V2`. Build bem-sucedido comprova compilação e verificações do APK, não compatibilidade com o firmware.

## API Android

- `DevicePolicyManager.createAndManageUser`
- `DevicePolicyManager.installExistingPackage`
- `DevicePolicyManager.switchUser`

Documentação: https://developer.android.com/reference/android/app/admin/DevicePolicyManager
