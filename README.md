# CLONE X

**CLONE X** é um aplicativo Android para gerar cópias instaláveis de aplicativos já presentes no aparelho. Cada cópia recebe outro identificador de pacote, instalação própria, ícone próprio e armazenamento privado separado pelo Android. Não usa contêiner de virtualização nem perfil de trabalho.

## Compilação

O GitHub Actions compila o APK automaticamente em cada envio para `main`. O artefato gerado chama-se **CLONE-X-APK**.

1. Abra a aba **Actions** deste repositório.
2. Entre na execução mais recente concluída.
3. Baixe o artefato **CLONE-X-APK**.

## Requisitos e compatibilidade

- Android 9 ou superior; a disponibilidade de recursos depende da versão e do fabricante.
- O Android sempre apresenta a confirmação de instalação quando exigida pelo sistema.
- Cópias são instalações separadas e começam com dados próprios.
- A alteração do pacote e da assinatura pode impedir o funcionamento de apps que dependam da assinatura original, Play Integrity, DRM, login Google ou verificações próprias. Não há garantia de compatibilidade universal.

## Base técnica e licença

O mecanismo de clonagem integrado nesta versão é baseado no projeto **CloniX / CloneForge**, de txorav, licenciado sob GNU GPL v3.0. O código-fonte correspondente está no submódulo `engine/CloniX`; as alterações de marca CLONE X são aplicadas pelo script `ci/brand.sh`.

- Projeto original: https://github.com/txorav/CloniX
- Licença: https://github.com/txorav/CloniX/blob/main/LICENSE

CLONE X não distribui APKs de terceiros; ele trabalha com aplicativos instalados no próprio dispositivo.
