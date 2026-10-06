package id.alluka.manager.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object AllukaEngine {
    private const val MODULE_PATH = "/data/adb/modules/alluka"
    private const val CONFIG_PATH = "/data/adb/.config/alluka"
    private const val PROFILE_FILE = "$CONFIG_PATH/profile"

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
