package com.darkk.compatcontrol.xposed

import android.content.Context
import com.darkk.compatcontrol.data.AppConfig
import com.darkk.compatcontrol.data.ConfigRepository
import com.google.gson.Gson
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CompatHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private lateinit var prefs: XSharedPreferences
    private val gson = Gson()

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        prefs = XSharedPreferences("com.darkk.compatcontrol", "compat_configs")
        prefs.makeWorldReadable()
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName

        // Ignora pacotes do sistema e o próprio app
        if (packageName == "com.darkk.compatcontrol") return
        if (packageName.startsWith("android")) return

        // Verifica se tem config salva pra esse pacote
        prefs.reload()
        val configJson = prefs.getString(packageName, null) ?: return

        val config = try {
            gson.fromJson(configJson, AppConfig::class.java)
        } catch (e: Exception) {
            return
        }

        // Aplica hooks apenas se tiver configuração relevante
        if (config.scale == 100 && !config.flagAspectRatio && !config.flagSandbox
            && !config.flagNonResize && !config.flagNavInsets) return

        XposedBridge.log("CompatControl: aplicando config para $packageName (scale=${config.scale}%)")

        hookWindowManager(lpparam, config)
    }

    private fun hookWindowManager(
        lpparam: XC_LoadPackage.LoadPackageParam,
        config: AppConfig
    ) {
        try {
            // Hook na Activity para aplicar densidade customizada se scale < 100
            if (config.scale < 100) {
                XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    lpparam.classLoader,
                    "onStart",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            try {
                                val activity = param.thisObject
                                val ctx = activity as Context
                                val resources = ctx.resources
                                val config2 = resources.configuration
                                val displayMetrics = resources.displayMetrics

                                // Aplica fator de escala nos display metrics
                                val scaleFactor = config.scale / 100f
                                displayMetrics.density *= scaleFactor
                                displayMetrics.densityDpi = (displayMetrics.densityDpi * scaleFactor).toInt()

                                XposedBridge.log("CompatControl: escala ${config.scale}% aplicada via hook em ${activity.javaClass.name}")
                            } catch (e: Exception) {
                                XposedBridge.log("CompatControl hook error: ${e.message}")
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            XposedBridge.log("CompatControl: falha ao hookear $lpparam.packageName: ${e.message}")
        }
    }
}
