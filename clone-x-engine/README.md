# CLONE X — engine de clonagem

## Engine atual: virtualização BlackBox

A engine CloniX/CloneForge foi descartada como base principal depois do teste no aparelho: os aplicativos clonados não iniciavam. O CLONE X passa a usar uma arquitetura diferente, baseada no motor de virtualização BlackBox/NewBlackbox, em vez de reescrever e reassinar cada APK.

- Base: https://github.com/ashiquesheriefofficial-max/MaxCloner
- Commit fixado: `fd3f410962f24a3ebbb4c26d2ed38f95c58de75e`
- Licença da base: Apache-2.0.
- Requisitos declarados pela base: Android 5.0+; ARM32 e ARM64.
- O Galaxy A7 com Android 10 está dentro da faixa de versão declarada, mas ainda exige teste físico.

## Como funciona

Os aplicativos são instalados e executados dentro de espaços virtuais isolados do motor BlackBox. Cada espaço mantém seus próprios dados e sessão. A mesma aplicação pode ser adicionada em mais de um espaço, sem criar APKs independentes com package names diferentes.

A base MaxCloner inclui suporte a split APKs (base + configurações de ABI/densidade), importante para apps modernos distribuídos pela Play Store.

## Limitações e validação

Virtualização não equivale a instalação Android nativa. Aplicativos que dependem de Play Integrity, assinatura original, Google Sign-In ou verificações anti-virtualização podem recusar execução. O próprio projeto-base alerta que alguns mensageiros podem detectar ambientes virtualizados; não há garantia de que o WhatsApp funcione ou permita múltiplas sessões.

Antes de anunciar o CLONE X como funcional, testar no Samsung Android 10: abrir o host, importar um app simples, iniciar e fechar o clone, criar dois espaços do WhatsApp, login independente, notificações, mídia, câmera, chamadas e reinicialização. Não tratar compilação bem-sucedida como prova de funcionamento.

## Build

O workflow `.github/workflows/clonex-profile.yml` baixa o commit fixado, aplica a marca CLONE X e compila as variantes de APK. O artefato será `CLONE-X-VIRTUAL-APK`.

O applicationId do host é `app.clonex`. O namespace interno `top.niunaijun.blackbox` deve permanecer inalterado porque é referenciado por código Java e JNI.

## Distribuição

Manter os avisos de licença Apache-2.0 e os créditos exigidos pela base. O CLONE X é uma adaptação e não deve ser apresentado como o projeto original MaxCloner.
