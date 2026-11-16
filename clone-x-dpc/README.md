# CLONE X — perfil Android isolado

Este diretório contém a implementação inicial do CLONE X baseada em **Android Managed Profiles** (perfil gerenciado), em vez de reempacotar o APK do aplicativo-alvo.

## Fluxo implementado

1. O usuário instala e abre CLONE X.
2. CLONE X verifica se o aparelho oferece suporte a perfis gerenciados.
3. O botão **CRIAR ESPAÇO SEPARADO** inicia o fluxo oficial de provisionamento do Android.
4. Depois que o Android concede a função de Profile Owner ao CLONE X, o usuário informa o application ID do app já instalado no perfil principal (por exemplo, `com.whatsapp`).
5. CLONE X chama `DevicePolicyManager.installExistingPackage()` para instalar a aplicação original no perfil gerenciado, preservando o APK e a assinatura do fornecedor. O Android mantém os dados do perfil separados.

## Compilação

O workflow `.github/workflows/clonex-profile.yml` compila este projeto e publica o artefato `CLONE-X-PROFILE-APK`.

## Estado honesto

Este é um protótipo de integração DPC/Managed Profile, não uma versão comercial validada. O build verifica compilação, não a criação real do perfil nem o funcionamento do WhatsApp. O teste funcional precisa ser feito em aparelhos Android físicos de fabricantes diferentes, incluindo provisionamento, instalação do pacote no perfil, inicialização, notificações, mídia, backup e remoção do perfil.

O Android/fabricante pode bloquear o provisionamento ou não oferecer `FEATURE_MANAGED_USERS`. O usuário precisa aceitar o fluxo do sistema. O ícone do app no launcher pode receber o indicador visual de perfil de trabalho do próprio Android.

## Próximas tarefas técnicas

- Validar provisionamento em Android 9–16 e em interfaces Samsung, Motorola e Xiaomi.
- Implementar descoberta de apps instalados sem depender de digitação manual do package ID.
- Implementar listagem e gerenciamento do perfil, remoção segura e tratamento de erros específicos do fabricante.
- Criar fluxo de onboarding, política de privacidade e suporte comercial.
- Testar WhatsApp oficial em um dispositivo físico antes de anunciar compatibilidade.
