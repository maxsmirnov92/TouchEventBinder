package net.maxsmr.toucheventbinder.keyadapter.trigger

import net.maxsmr.toucheventbinder.config.KeyConfig

interface KeyActionTrigger {

    fun doActionWithKey(keyCode: Int, config: KeyConfig): Boolean

    fun release()
}