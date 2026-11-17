# CLONE X — validação

## T0 — build e integridade
- GitHub Actions deve concluir o job Build e Verify.
- O artefato só é publicado depois de `verify.sh` passar.
- Verificar assinatura, package ID, SDKs, receiver Device Admin e classes DEX.

## T1 — instalação normal
- Instalar o APK em um Samsung SM-A750G com Android 10.
- Abrir CLONE X e confirmar que ele não solicita permissões de armazenamento desnecessárias.

## T2 — suporte do firmware
- Conferir se o aparelho anuncia suporte a usuários gerenciados.
- Não interpretar a feature como garantia de que a Samsung permite criar usuários adicionais.

## T3 — Device Owner
- Não redefinir o telefone para este teste.
- Com depuração USB, tentar o comando mostrado no app apenas se o aparelho estiver elegível.
- Se o Android recusar por contas, configuração concluída ou política Samsung, interromper. Não apagar dados para contornar.

## T4 — criar espaço
- Como Device Owner, criar um espaço.
- Confirmar que o Android retorna um UserHandle e que o serial é salvo.
- Confirmar a troca para o novo usuário.

## T5 — afiliação e instalação
- No usuário criado, abrir CLONE X.
- Confirmar que o app está como Profile Owner e que `isAffiliatedUser` retorna true.
- Informar um pacote já instalado no aparelho, por exemplo `com.whatsapp`.
- PASS: `installExistingPackage` retorna true e `getPackageInfo` confirma o pacote no usuário atual.

## T6 — isolamento
- Abrir o app instalado no espaço.
- Confirmar que login, dados e notificações são independentes.
- Testar também um segundo app que esteja instalado no usuário principal.

## T7 — reinício e remoção
- Reiniciar o telefone e confirmar que os usuários continuam listados.
- Confirmar que o Device Owner ainda consegue alternar para o espaço.
- Remover apenas um espaço de teste e confirmar que os dados daquele usuário são removidos.

## Limite conhecido
O APK não transforma um app em cópia universal por si só. O método depende de o Android/Samsung permitir usuário gerenciado, Device Owner, afiliação e instalação do pacote existente. A aprovação do build não comprova esses requisitos no aparelho físico.
