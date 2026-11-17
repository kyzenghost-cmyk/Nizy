package app.clonex.profile

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.os.UserHandle
import android.os.UserManager
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var dpm: DevicePolicyManager
    private lateinit var admin: ComponentName
    private lateinit var root: LinearLayout
    private lateinit var packageInput: EditText
    private val prefs by lazy { getSharedPreferences("clone_x_users", Context.MODE_PRIVATE) }
    private val userManager by lazy { getSystemService(Context.USER_SERVICE) as UserManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, CloneXAdminReceiver::class.java)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized) render()
    }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 32, 28, 28)
        }
        root.addView(TextView(this).apply { text = "CLONE X"; textSize = 30f })
        val status = TextView(this).apply { textSize = 14f; setPadding(0, 16, 0, 18) }
        root.addView(status)

        when {
            dpm.isDeviceOwnerApp(packageName) -> renderDeviceOwner(status)
            dpm.isProfileOwnerApp(packageName) -> renderCloneSpace(status)
            else -> renderNotProvisioned(status)
        }
        setContentView(root)
    }

    private fun renderNotProvisioned(status: TextView) {
        status.text = "Ativação necessária"
        root.addView(TextView(this).apply {
            text = "Para criar usuários Android independentes, o CLONE X precisa ser Device Owner. A ativação é feita por ADB; não existe botão capaz de conceder esse privilégio."
            textSize = 15f
        })
        root.addView(TextView(this).apply {
            text = "Não faça redefinição de fábrica por causa deste app. Primeiro tente o comando abaixo com depuração USB. Se o Android recusar por causa da configuração/contas existentes, pare e envie a mensagem exata; não apague os dados do telefone."
            textSize = 14f
            setPadding(0, 18, 0, 18)
        })
        root.addView(TextView(this).apply {
            text = "adb shell dpm set-device-owner $packageName/app.clonex.profile.CloneXAdminReceiver"
            textSize = 13f
        })
        root.addView(Button(this).apply {
            text = "VERIFICAR SUPORTE"
            setOnClickListener {
                val feature = packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)
                val max = userManager.userProfiles.size
                toast("Managed users: $feature | perfis visíveis: $max")
            }
        }, match())
    }

    private fun renderDeviceOwner(status: TextView) {
        status.text = "Device Owner ativo. Gerenciamento do aparelho habilitado."
        // O Device Owner e cada Profile Owner precisam compartilhar ao menos
        // um affiliation ID para operações permitidas entre usuários.
        runCatching { dpm.setAffiliationIds(admin, setOf(AFFILIATION_ID)) }
            .onFailure { toast("Não foi possível configurar afiliação: ${it.message}") }

        root.addView(Button(this).apply {
            text = "CRIAR NOVO ESPAÇO"
            setOnClickListener { createCloneUser() }
        }, match())
        root.addView(TextView(this).apply {
            text = "Cada espaço é um usuário Android separado. O firmware Samsung pode limitar ou bloquear a criação."
            textSize = 13f
            setPadding(0, 16, 0, 12)
        })
        val serials = prefs.getStringSet("serials", emptySet())!!.toList().sorted()
        if (serials.isEmpty()) {
            root.addView(TextView(this).apply { text = "Nenhum espaço criado."; textSize = 14f })
        }
        serials.forEachIndexed { index, serial ->
            val handle = userManager.getUserForSerialNumber(serial.toLongOrNull() ?: -1L)
            if (handle != null) {
                root.addView(Button(this).apply {
                    text = "ABRIR ESPAÇO ${index + 1}"
                    setOnClickListener { switchTo(handle) }
                }, match())
            }
        }
    }

    private fun renderCloneSpace(status: TextView) {
        status.text = "Espaço independente ativo."
        val affiliation = runCatching {
            dpm.setAffiliationIds(admin, setOf(AFFILIATION_ID))
            dpm.isAffiliatedUser
        }.getOrDefault(false)
        root.addView(TextView(this).apply {
            text = if (affiliation) "Afiliação com o dispositivo confirmada." else
                "Afiliação ainda não confirmada. A instalação pode ser recusada pelo Android."
            textSize = 13f
        })
        packageInput = EditText(this).apply {
            hint = "Pacote do app (ex.: com.whatsapp)"
            setSingleLine(true)
        }
        root.addView(packageInput, match())
        root.addView(Button(this).apply {
            text = "INSTALAR APP NESTE ESPAÇO"
            setOnClickListener { installIntoThisUser(packageInput.text.toString().trim()) }
        }, match())
        root.addView(TextView(this).apply {
            text = "O app precisa já estar instalado no aparelho. A instalação ocorre no usuário atual e mantém os dados/login separados."
            textSize = 13f
            setPadding(0, 18, 0, 0)
        })
    }

    private fun createCloneUser() {
        if (!dpm.isDeviceOwnerApp(packageName)) {
            toast("CLONE X não é Device Owner.")
            return
        }
        val serials = prefs.getStringSet("serials", emptySet())!!.toMutableSet()
        if (serials.size >= MAX_SPACES) {
            toast("Limite local de $MAX_SPACES espaços atingido.")
            return
        }
        try {
            dpm.setAffiliationIds(admin, setOf(AFFILIATION_ID))
            val user = dpm.createAndManageUser(
                admin,
                "CLONE X ${serials.size + 1}",
                admin,
                PersistableBundle(),
                DevicePolicyManager.SKIP_SETUP_WIZARD
            ) ?: run {
                toast("Android/Samsung recusou a criação do usuário.")
                return
            }
            val serial = userManager.getSerialNumberForUser(user)
            if (serial < 0L) error("Não foi possível obter o serial do usuário criado.")
            serials.add(serial.toString())
            prefs.edit().putStringSet("serials", serials).apply()
            if (!switchTo(user)) {
                toast("Espaço criado. Use o seletor de usuários do Android para entrar.")
            }
        } catch (e: SecurityException) {
            toast("Permissão negada: confirme Device Owner e as regras do firmware.")
        } catch (e: Exception) {
            toast("Falha ao criar espaço: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun switchTo(user: UserHandle): Boolean = try {
        val ok = dpm.switchUser(admin, user)
        if (!ok) toast("Android recusou a troca de usuário.")
        ok
    } catch (e: Exception) {
        toast("Falha ao trocar de usuário: ${e.message ?: e.javaClass.simpleName}")
        false
    }

    private fun installIntoThisUser(targetPackage: String) {
        if (!dpm.isProfileOwnerApp(packageName)) {
            toast("Este usuário não está administrado como Profile Owner.")
            return
        }
        if (targetPackage.isBlank() || targetPackage == packageName) {
            toast("Informe o pacote do aplicativo, por exemplo com.whatsapp.")
            return
        }
        try {
            // Reaplica a afiliação no próprio Profile Owner; o Device Owner
            // define o mesmo ID antes de criar o usuário.
            dpm.setAffiliationIds(admin, setOf(AFFILIATION_ID))
            if (!dpm.isAffiliatedUser) {
                error("Usuário não afiliado ao Device Owner; instalação bloqueada pelo Android.")
            }
            val installed = dpm.installExistingPackage(admin, targetPackage)
            if (!installed) error("Package Manager não disponibilizou $targetPackage neste usuário.")
            // Não reporta sucesso antes de consultar o PackageManager local.
            packageManager.getPackageInfo(targetPackage, 0)
            toast("Instalado e confirmado neste espaço: $targetPackage")
            packageManager.getLaunchIntentForPackage(targetPackage)?.let { startActivity(it) }
                ?: toast("Instalado, mas sem atividade de inicialização.")
        } catch (e: Exception) {
            toast("Falha ao instalar: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun match() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = 12 }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    companion object {
        private const val MAX_SPACES = 10
        private const val AFFILIATION_ID = "clonex-device-affiliation-v1"
    }
}
