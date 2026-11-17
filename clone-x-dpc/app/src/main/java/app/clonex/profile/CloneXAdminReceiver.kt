package app.clonex.profile

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class CloneXAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, CloneXAdminReceiver::class.java)
        try {
            dpm.setAffiliationIds(admin, setOf("clonex-device-affiliation-v1"))
        } catch (e: Exception) {
            Log.e("CLONE-X", "Could not set profile affiliation", e)
        }
    }
}
