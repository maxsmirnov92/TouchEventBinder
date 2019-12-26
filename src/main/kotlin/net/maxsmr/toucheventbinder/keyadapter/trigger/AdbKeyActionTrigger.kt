package net.maxsmr.toucheventbinder.keyadapter.trigger

import net.maxsmr.commonutils.shell.ShellWrapper
import net.maxsmr.toucheventbinder.config.KeyConfig
import net.maxsmr.toucheventbinder.keyadapter.KeyBinder

object AdbKeyActionTrigger: KeyActionTrigger {

    private val shellWrapper = ShellWrapper(addToCommandsMap = false)

    override fun doActionWithKey(keyCode: Int, config: KeyConfig): Boolean {
        config.touchPosition?.let {
            if (it.isValid()) {
                return shellWrapper.executeCommand(listOf("adb", "shell", "input", "tap", it.x.toString(), it.y.toString())).isSuccessful
            }
        }
        return false
    }

    override fun release() {
        shellWrapper.dispose()
    }
}