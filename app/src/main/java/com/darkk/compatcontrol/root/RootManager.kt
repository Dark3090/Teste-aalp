package com.darkk.compatcontrol.root

import com.darkk.compatcontrol.data.AppConfig
import com.topjohnwu.superuser.Shell

object RootManager {

    private val SCALE_FLAGS = listOf(30, 40, 50, 60, 70, 80, 90)

    fun init() {
        Shell.enableVerboseLogging = false
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    fun isRootAvailable(): Boolean {
        return Shell.isAppGrantedRoot() == true
    }

    fun applyConfig(config: AppConfig): Result<Unit> {
        return try {
            val cmds = mutableListOf<String>()
            val pkg = config.packageName

            // Remove old scale flags
            SCALE_FLAGS.forEach { v ->
                cmds.add("am compat disable DOWNSCALE_$v $pkg")
            }
            cmds.add("am compat disable DOWNSCALED $pkg")

            // Apply new scale
            if (config.scale < 100) {
                cmds.add("am compat enable DOWNSCALED $pkg")
                cmds.add("am compat enable DOWNSCALE_${config.scale} $pkg")
            }

            // Apply flags
            config.toFlags().forEach { (flag, enabled) ->
                val action = if (enabled) "enable" else "disable"
                cmds.add("am compat $action $flag $pkg")
            }

            val result = Shell.cmd(*cmds.toTypedArray()).exec()
            if (result.isSuccess) Result.success(Unit)
            else Result.failure(Exception(result.err.joinToString("\n")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resetConfig(packageName: String): Result<Unit> {
        return try {
            val cmds = mutableListOf<String>()

            SCALE_FLAGS.forEach { v ->
                cmds.add("am compat disable DOWNSCALE_$v $packageName")
            }
            cmds.add("am compat disable DOWNSCALED $packageName")
            cmds.add("am compat disable OVERRIDE_MIN_ASPECT_RATIO $packageName")
            cmds.add("am compat disable NEVER_SANDBOX_DISPLAY_APIS $packageName")
            cmds.add("am compat disable FORCE_NON_RESIZE_APP $packageName")
            cmds.add("am compat disable ALLOW_IGNORING_NAVIGATION_BAR_INSETS $packageName")

            val result = Shell.cmd(*cmds.toTypedArray()).exec()
            if (result.isSuccess) Result.success(Unit)
            else Result.failure(Exception(result.err.joinToString("\n")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMemInfo(packageName: String): String? {
        return try {
            val result = Shell.cmd("dumpsys meminfo $packageName | grep TOTAL").exec()
            result.out.firstOrNull()?.trim()
        } catch (e: Exception) { null }
    }

    fun isAppRunning(packageName: String): Boolean {
        return try {
            val result = Shell.cmd("pidof $packageName").exec()
            result.out.firstOrNull()?.trim()?.isNotEmpty() == true
        } catch (e: Exception) { false }
    }
}
