package app.clonex.profile

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
    private lateinit var status: TextView
    private lateinit var packageInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, CloneXAdminReceiver::class.java)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 28)
        }
        root.addView(TextView(this).apply { text = "CLONE X"; textSize = 30f })
        status = TextView(this).apply { textSize = 15f; setPadding(0, 20, 0, 20) }
        root.addView(status)
        root.addView(Button(this).apply {
            text = "CRIAR ESPAÇO SEPARADO"
            setOnClickListener { startProfileProvisioning() }
        }, match())
        packageInput = EditText(this).apply {
            hint = "Pacote do app (ex.: com.whatsapp)"
            setSingleLine(true)
        }
        root.addView(packageInput, match())
        root.addView(Button(this).apply {
            text = "ADICIONAR APP AO ESPAÇO"
            setOnClickListener { addExistingApp(packageInput.text.toString().trim()) }
        }, match())
        root.addView(TextView(this).apply {
            text = "O Android cria um perfil isolado. O app original é instalado nesse perfil sem reempacotar nem alterar seu APK."
            textSize = 13f
            setPadding(0, 24, 0, 0)
        })
        setContentView(root)
        updateStatus()
    }

    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }

    private fun updateStatus() {
        val owner = dpm.isProfileOwnerApp(packageName)
        status.text = if (owner) "Espaço separado ativo neste perfil." else "Espaço ainda não criado neste aparelho."
    }

    private fun startProfileProvisioning() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) {
            toast("Este aparelho não oferece suporte a perfis gerenciados.")
            return
        }
        if (dpm.isProfileOwnerApp(packageName)) {
            toast("O espaço CLONE X já está ativo.")
            return
        }
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)
        }
        try {
            startActivityForResult(intent, REQUEST_PROVISION)
        } catch (_: Exception) {
            toast("O Android não aceitou iniciar a criação do perfil neste aparelho.")
        }
    }

    private fun addExistingApp(packageNameToAdd: String) {
        if (!dpm.isProfileOwnerApp(packageName)) {
            toast("Crie o espaço separado primeiro.")
            return
        }
        if (packageNameToAdd.isBlank() || packageNameToAdd == packageName) {
            toast("Informe o pacote do aplicativo.")
            return
        }
        try {
            val installed = dpm.installExistingPackage(admin, packageNameToAdd)
            if (!installed) {
                toast("O Android não conseguiu instalar esse app no perfil. Confirme que ele está instalado no perfil principal.")
                return
            }
            val launch = packageManager.getLaunchIntentForPackage(packageNameToAdd)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launch)
            } else {
                toast("App adicionado ao perfil, mas não foi encontrado um atalho de abertura.")
            }
        } catch (e: SecurityException) {
            toast("Permissão de administrador do perfil não concedida pelo Android.")
        } catch (e: Exception) {
            toast("Falha ao adicionar app: ${e.javaClass.simpleName}")
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    companion object { private const val REQUEST_PROVISION = 4102 }
}
