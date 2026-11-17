# CLONE X — engine de clonagem

O protótipo anterior baseado em Managed Profile foi arquivado como experimento e não é o motor principal do CLONE X.

## Engine integrada

O workflow compila o **CloniX / CloneForge** como motor de clonagem, fixado no commit:

- Repositório original: https://github.com/txorav/CloniX
- Commit fixado: `da50e8a9f7d141460a9fa0957b3378b65dbca606`
- Licença: GPL-3.0 (o código derivado e as modificações distribuídas devem respeitar a GPL-3.0).
- Compatibilidade declarada pelo upstream: Android 9+ (API 28–36), ARM64 e ARM32.

O código-fonte upstream é obtido pelo workflow em `clone-x-engine/upstream`, e o workflow aplica a marca CLONE X e o applicationId `app.clonex` antes de compilar. O código upstream não é copiado para este diretório; a referência acima identifica exatamente a versão utilizada.

## O que a engine faz

- Lê APK base e splits instalados.
- Reescreve identificador do pacote, authorities e referências do manifesto.
- Injeta runtime de compatibilidade Java e hooks nativos.
- Assina as cópias com identidade persistente no dispositivo.
- Instala cada clone como pacote Android separado, com UID e sandbox de dados próprios.
- Permite criar múltiplas instâncias numeradas.

## Limitações que precisam ser testadas

A alteração de package name e assinatura não é uma cópia criptograficamente idêntica. O runtime tenta compatibilizar verificações e referências internas, mas não pode garantir que todo aplicativo aceite a cópia. Serviços vinculados à identidade original, Play Integrity, autenticação, notificações push, chamadas e verificações nativas podem falhar.

A engine upstream declara suporte a Android 9+ e arquiteturas ARM32/ARM64. Isso não equivale a compatibilidade comprovada com todos os aparelhos ou aplicativos. O WhatsApp ainda precisa ser testado fisicamente em pelo menos cinco instâncias: instalação, abertura, login independente, notificações, mídia, câmera, chamadas e reinicialização.

## Build

O workflow `.github/workflows/clonex-profile.yml` compila o runtime, a ponte nativa e o APK CLONE X. O artefato se chama `CLONE-X-APK`.

A compilação do APK não comprova funcionamento do WhatsApp. Não anunciar como produto funcional antes dos testes em aparelho físico.

## Aviso de distribuição

CloniX é GPL-3.0. Antes de distribuir versões modificadas, publicar ou disponibilizar aos destinatários o código-fonte correspondente completo, scripts de build e modificações, manter avisos de copyright/licença e cumprir os demais termos da GPL-3.0. Não remover os créditos do projeto original.
