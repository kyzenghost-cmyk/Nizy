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
    private lateinit var status: TextView
    private lateinit var packageInput: EditText
    private val prefs by lazy { getSharedPreferences("clone_x_users", Context.MODE_PRIVATE) }

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
        status = TextView(this).apply { textSize = 14f; setPadding(0, 16, 0, 18) }
        root.addView(status)

        when {
            dpm.isDeviceOwnerApp(packageName) -> renderDeviceOwner()
            dpm.isProfileOwnerApp(packageName) -> renderCloneSpace()
            else -> renderNotProvisioned()
        }
        setContentView(root)
    }

    private fun renderNotProvisioned() {
        status.text = "Configuração inicial necessária"
        root.addView(TextView(this).apply {
            text = "Para criar vários usuários Android independentes, o CLONE X precisa ser Administrador do dispositivo (Device Owner). O Android só permite ativar isso durante a configuração inicial do aparelho ou em um aparelho de teste preparado para provisionamento."
            textSize = 15f
        })
        root.addView(TextView(this).apply {
            text = "ATENÇÃO: o procedimento de produção exige redefinição de fábrica. Faça backup antes. Não execute se não quiser apagar os dados do telefone."
            textSize = 14f
            setPadding(0, 18, 0, 18)
        })
        root.addView(TextView(this).apply {
            text = "Com ADB em um aparelho de teste recém-redefinido, depois de instalar o APK, o comando de desenvolvimento é:\n\nadb shell dpm set-device-owner app.clonex.profile/.CloneXAdminReceiver"
            textSize = 13f
        })
        root.addView(Button(this).apply {
            text = "VERIFICAR SUPORTE A MÚLTIPLOS USUÁRIOS"
            setOnClickListener {
                val um = getSystemService(Context.USER_SERVICE) as UserManager
                val feature = packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)
                toast("Managed users: $feature | limite informado: ${um.userProfiles.size} perfil(is) visível(is).")
            }
        }, match())
    }

    private fun renderDeviceOwner() {
        status.text = "Administrador principal ativo — este aparelho está gerenciado pelo CLONE X."
        root.addView(Button(this).apply {
            text = "CRIAR NOVO ESPAÇO / CÓPIA"
            setOnClickListener { createCloneUser() }
        }, match())
        root.addView(TextView(this).apply {
            text = "Cada espaço é um usuário Android real, com dados e sessão separados. O limite depende do firmware Samsung e da memória disponível."
            textSize = 13f
            setPadding(0, 16, 0, 12)
        })
        val serials = prefs.getStringSet("serials", emptySet())!!.toList().sorted()
        if (serials.isEmpty()) {
            root.addView(TextView(this).apply { text = "Nenhum espaço criado ainda."; textSize = 14f })
        }
        serials.forEachIndexed { index, serial ->
            val handle = (getSystemService(Context.USER_SERVICE) as UserManager)
                .getUserForSerialNumber(serial.toLongOrNull() ?: -1L)
            if (handle != null) {
                root.addView(Button(this).apply {
                    text = "ABRIR ESPAÇO ${index + 1}"
                    setOnClickListener { switchTo(handle) }
                }, match())
            }
        }
    }

    private fun renderCloneSpace() {
        status.text = "Espaço independente ativo. Instale o app original neste usuário."
        packageInput = EditText(this).apply {
            hint = "Pacote do app (ex.: com.whatsapp)"
            setSingleLine(true)
        }
        root.addView(packageInput, match())
        root.addView(Button(this).apply {
            text = "INSTALAR APP ORIGINAL NESTE ESPAÇO"
            setOnClickListener { installIntoThisUser(packageInput.text.toString().trim()) }
        }, match())
        root.addView(TextView(this).apply {
            text = "Depois da instalação, o app abre com armazenamento, login e dados separados dos outros usuários. Para trocar de espaço, use o seletor de usuários do próprio Android/Samsung."
            textSize = 13f
            setPadding(0, 18, 0, 0)
        })
    }

    private fun createCloneUser() {
        if (!dpm.isDeviceOwnerApp(packageName)) {
            toast("O CLONE X não é Device Owner.")
            return
        }
        val serials = prefs.getStringSet("serials", emptySet())!!.toMutableSet()
        if (serials.size >= MAX_SPACES) {
            toast("Limite do CLONE X atingido: $MAX_SPACES espaços. O Android/Samsung ainda pode impor um limite menor.")
            return
        }
        try {
            val index = serials.size + 1
            val user = dpm.createAndManageUser(
                admin,
                "CLONE X $index",
                admin,
                PersistableBundle(),
                DevicePolicyManager.SKIP_SETUP_WIZARD
            )
            if (user == null) {
                toast("O Android recusou a criação. O firmware pode ter atingido o limite de usuários.")
                return
            }
            val serial = (getSystemService(Context.USER_SERVICE) as UserManager).getSerialNumberForUser(user)
            serials.add(serial.toString())
            prefs.edit().putStringSet("serials", serials).apply()
            if (!switchTo(user)) toast("Espaço criado, mas o Android não permitiu alternar automaticamente. Use o seletor de usuários.")
        } catch (e: SecurityException) {
            toast("Permissão negada. O CLONE X precisa ser Device Owner.")
        } catch (e: Exception) {
            toast("Falha ao criar espaço: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun switchTo(user: UserHandle): Boolean {
        return try {
            val ok = dpm.switchUser(admin, user)
            if (!ok) toast("O Android recusou a troca de usuário.")
            ok
        } catch (e: Exception) {
            toast("Não foi possível trocar de usuário: ${e.javaClass.simpleName}")
            false
        }
    }

    private fun installIntoThisUser(targetPackage: String) {
        if (!dpm.isProfileOwnerApp(packageName)) {
            toast("Este espaço não está administrado pelo CLONE X.")
            return
        }
        if (targetPackage.isBlank() || targetPackage == packageName) {
            toast("Informe o pacote do aplicativo que já está instalado no usuário principal.")
            return
        }
        try {
            if (!dpm.installExistingPackage(admin, targetPackage)) {
                toast("O Android não encontrou esse app instalado em outro usuário.")
                return
            }
            val launch = packageManager.getLaunchIntentForPackage(targetPackage)
            if (launch == null) {
                toast("Instalado, mas sem atividade de inicialização.")
                return
            }
            startActivity(launch)
        } catch (e: Exception) {
            toast("Falha ao instalar neste espaço: ${e.message ?: e.javaClass.simpleName}")
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
    }
}
