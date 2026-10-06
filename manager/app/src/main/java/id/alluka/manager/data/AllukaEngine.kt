package id.alluka.manager.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object AllukaEngine {
    private const val MODULE_PATH = "/data/adb/modules/alluka"
    private const val CONFIG_PATH = "/data/adb/.config/alluka"
    private const val PROFILE_FILE = "$CONFIG_PATH/profile"
    private const val TWEAKS_FILE = "$CONFIG_PATH/tweaks.prop"
    private const val LOG_FILE = "$CONFIG_PATH/alluka.log"

    suspend fun getActiveProfile(): String = withContext(Dispatchers.IO) {
        val result = execRoot("cat $PROFILE_FILE 2>/dev/null")
        if (result.trim().isNotEmpty()) result.trim() else "daily"
    }

    suspend fun applyProfile(profile: String): Boolean = withContext(Dispatchers.IO) {
        val cmd = "mkdir -p $CONFIG_PATH && echo '$profile' > $PROFILE_FILE && sh $MODULE_PATH/apply.sh $profile"
        val output = execRoot(cmd)
        output.contains("applied successfully") || checkProfileFile(profile)
    }

    private suspend fun checkProfileFile(expected: String): Boolean = withContext(Dispatchers.IO) {
        val current = execRoot("cat $PROFILE_FILE 2>/dev/null").trim()
        current == expected
    }

    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        val res = execRoot("id")
        res.contains("uid=0(root)")
    }

    suspend fun isModuleInstalled(): Boolean = withContext(Dispatchers.IO) {
        val res = execRoot("[ -f $MODULE_PATH/module.prop ] && echo '1' || echo '0'")
        res.trim() == "1"
    }

    // Tweak properties reader & writer (Alluka & AZenith Engine Integration)
    suspend fun getTweakProperty(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        val out = execRoot("grep '^$key=' $TWEAKS_FILE 2>/dev/null | cut -d'=' -f2")
        if (out.trim().isNotEmpty()) out.trim() else defaultValue
    }

    suspend fun setTweakProperty(key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        val cmd = "mkdir -p $CONFIG_PATH; touch $TWEAKS_FILE; " +
                "sed -i '/^$key=/d' $TWEAKS_FILE; echo '$key=$value' >> $TWEAKS_FILE"
        execRoot(cmd)
        true
    }

    // Mode Settings (Governor & I/O Scheduler per mode: Sleep, Daily, Performance, Auto)
    suspend fun getModeGovernor(mode: String, defaultGov: String = "schedutil"): String = withContext(Dispatchers.IO) {
        getTweakProperty("gov_$mode", defaultGov)
    }

    suspend fun setModeGovernor(mode: String, governor: String): Boolean = withContext(Dispatchers.IO) {
        setTweakProperty("gov_$mode", governor)
    }

    suspend fun getModeIo(mode: String, defaultIo: String = "bfq"): String = withContext(Dispatchers.IO) {
        getTweakProperty("io_$mode", defaultIo)
    }

    suspend fun setModeIo(mode: String, io: String): Boolean = withContext(Dispatchers.IO) {
        setTweakProperty("io_$mode", io)
    }

    // MediaTek GED Boost Control (Alluka Feature)
    suspend fun setGedBoost(boostEnable: Boolean, gpuEnable: Boolean, cpuEnable: Boolean, gpuBoost: Boolean): Boolean = withContext(Dispatchers.IO) {
        val b1 = if (boostEnable) "1" else "0"
        val b2 = if (gpuEnable) "1" else "0"
        val b3 = if (cpuEnable) "1" else "0"
        val b4 = if (gpuBoost) "1" else "0"
        val cmd = """
            echo $b1 > /sys/module/ged/parameters/ged_boost_enable 2>/dev/null
            echo $b2 > /sys/module/ged/parameters/boost_gpu_enable 2>/dev/null
            echo $b3 > /sys/module/ged/parameters/enable_cpu_boost 2>/dev/null
            echo $b4 > /sys/module/ged/parameters/enable_gpu_boost 2>/dev/null
        """.trimIndent()
        execRoot(cmd)
        setTweakProperty("ged_boost_enable", b1)
        setTweakProperty("boost_gpu_enable", b2)
        setTweakProperty("enable_cpu_boost", b3)
        setTweakProperty("enable_gpu_boost", b4)
    }

    // Render Engine (AZenith Feature)
    suspend fun setRenderEngine(renderer: String): Boolean = withContext(Dispatchers.IO) {
        val r = renderer.lowercase()
        val propVal = when (r) {
            "vulkan" -> "vulkan"
            "opengl" -> "opengl"
            "skiavk" -> "skiavk"
            "skiavkthreaded" -> "skiavkthreaded"
            else -> "default"
        }
        execRoot("setprop debug.hwui.renderer $propVal")
        setTweakProperty("render_engine", propVal)
    }

    // Refresh Rates (AZenith Feature)
    suspend fun setRefreshRate(hz: String): Boolean = withContext(Dispatchers.IO) {
        val mode = when (hz.lowercase().replace(" ", "").replace("hz", "")) {
            "90" -> "90"
            "120" -> "120"
            else -> "60"
        }
        execRoot("settings put system peak_refresh_rate $mode.0; settings put system min_refresh_rate $mode.0")
        setTweakProperty("refresh_rate", mode)
    }

    // Bypass Charging Control
    suspend fun setBypassCharging(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val nodeVal = if (enabled) "0" else "1"
        execRoot("for n in /sys/class/power_supply/battery/charging_enabled /sys/class/power_supply/battery/input_suspend; do [ -w \$n ] && echo $nodeVal > \$n; done")
        setTweakProperty("bypass_charging", if (enabled) "1" else "0")
    }

    // Diagnostics Log Reader (Alluka Feature)
    suspend fun getDiagnosticsLog(): String = withContext(Dispatchers.IO) {
        val out = execRoot("tail -n 60 $LOG_FILE 2>/dev/null")
        if (out.trim().isNotEmpty()) out.trim() else "Belum ada log aktif. Alluka Engine siap dijalankan."
    }

    // Real Daemon PID Detection
    suspend fun getDaemonPid(): String = withContext(Dispatchers.IO) {
        val pidFromFile = execRoot("cat $CONFIG_PATH/service.pid 2>/dev/null").trim()
        if (pidFromFile.isNotEmpty() && pidFromFile.matches(Regex("\\d+"))) {
            return@withContext pidFromFile
        }
        val pgrepResult = execRoot("pgrep -f 'alluka' 2>/dev/null | head -n 1").trim()
        if (pgrepResult.isNotEmpty() && pgrepResult.matches(Regex("\\d+"))) {
            return@withContext pgrepResult
        }
        val pidofResult = execRoot("pidof alluka-service 2>/dev/null | awk '{print \$1}'").trim()
        if (pidofResult.isNotEmpty() && pidofResult.matches(Regex("\\d+"))) {
            return@withContext pidofResult
        }
        "Offline"
    }

    // Real Hardware & System Information
    data class DeviceInfo(
        val deviceName: String,
        val chipset: String,
        val kernelVersion: String,
        val allukaVersion: String
    )

    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        // 1. Nama Perangkat (ro.product.marketname atau Build.MODEL)
        val marketName = execRoot("getprop ro.product.marketname 2>/dev/null").trim()
        val model = android.os.Build.MODEL ?: "Android Device"
        val brand = android.os.Build.MANUFACTURER ?: ""
        val deviceName = when {
            marketName.isNotEmpty() -> marketName
            brand.isNotEmpty() && !model.startsWith(brand, ignoreCase = true) -> "$brand $model"
            else -> model
        }

        // 2. Chipset / SoC (ro.soc.model atau ro.board.platform atau /proc/cpuinfo)
        val socModel = execRoot("getprop ro.soc.model 2>/dev/null").trim()
        val boardPlatform = execRoot("getprop ro.board.platform 2>/dev/null").trim()
        val cpuHardware = execRoot("grep -i 'Hardware' /proc/cpuinfo 2>/dev/null | cut -d':' -f2").trim()
        val chipset = when {
            socModel.isNotEmpty() -> socModel
            boardPlatform.equals("mt6769", ignoreCase = true) || boardPlatform.equals("mt6769z", ignoreCase = true) -> "Helio G85"
            boardPlatform.equals("mt6785", ignoreCase = true) -> "Helio G90T"
            boardPlatform.equals("sm8250", ignoreCase = true) -> "Snapdragon 865"
            boardPlatform.equals("sm8150", ignoreCase = true) -> "Snapdragon 855"
            boardPlatform.isNotEmpty() -> boardPlatform.uppercase()
            cpuHardware.isNotEmpty() -> cpuHardware
            else -> android.os.Build.HARDWARE ?: "ARM64 SoC"
        }

        // 3. Versi Kernel (uname -r atau /proc/version)
        val unameKernel = execRoot("uname -r 2>/dev/null").trim()
        val procVersion = execRoot("cat /proc/version 2>/dev/null | awk '{print \$3}'").trim()
        val sysKernel = System.getProperty("os.version") ?: "4.14"
        val kernelVersion = when {
            unameKernel.isNotEmpty() -> "Linux $unameKernel"
            procVersion.isNotEmpty() -> "Linux $procVersion"
            else -> "Linux $sysKernel"
        }

        // 4. Versi Alluka dari module.prop
        val propVersion = execRoot("grep '^version=' $MODULE_PATH/module.prop 2>/dev/null | cut -d'=' -f2").trim()
        val allukaVersion = if (propVersion.isNotEmpty()) "$propVersion Stable" else "v1.0 Stable"

        DeviceInfo(
            deviceName = deviceName,
            chipset = chipset,
            kernelVersion = kernelVersion,
            allukaVersion = allukaVersion
        )
    }

    private fun execRoot(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            os.write((command + "\nexit\n").toByteArray())
            os.flush()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            ""
        }
    }
}

