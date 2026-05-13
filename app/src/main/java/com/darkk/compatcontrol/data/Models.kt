package com.darkk.compatcontrol.data

data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean
)

data class AppConfig(
    val packageName: String,
    val scale: Int = 100,
    val flagAspectRatio: Boolean = false,
    val flagSandbox: Boolean = false,
    val flagNonResize: Boolean = false,
    val flagNavInsets: Boolean = false
) {
    fun toFlags(): List<Pair<String, Boolean>> = listOf(
        "OVERRIDE_MIN_ASPECT_RATIO" to flagAspectRatio,
        "NEVER_SANDBOX_DISPLAY_APIS" to flagSandbox,
        "FORCE_NON_RESIZE_APP" to flagNonResize,
        "ALLOW_IGNORING_NAVIGATION_BAR_INSETS" to flagNavInsets
    )
}

object ScaleValues {
    val options = listOf(30, 40, 50, 60, 70, 80, 90, 100)

    fun label(scale: Int) = when (scale) {
        30 -> "Extremo"
        40 -> "Muito leve"
        50 -> "Leve"
        60 -> "Econômico"
        70 -> "Balanceado"
        80 -> "Suave"
        90 -> "Equilibrado"
        100 -> "Nativo"
        else -> "Personalizado"
    }

    fun resolution(scale: Int, w: Int = 1080, h: Int = 2400): String {
        val sw = (w * scale / 100)
        val sh = (h * scale / 100)
        return "${sw}×${sh}"
    }
}
