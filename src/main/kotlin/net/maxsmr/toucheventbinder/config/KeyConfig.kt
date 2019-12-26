package net.maxsmr.toucheventbinder.config

import com.google.gson.annotations.SerializedName
import net.maxsmr.toucheventbinder.keyadapter.KeyEventListener

data class KeyConfig(
        @SerializedName("keyEventType")
        val keyEventType: KeyEventListener.ExtendedKeyEventType?,
        @SerializedName("interval")
        val interval: Long?,
        @SerializedName("touchPosition")
        val touchPosition: TouchPosition?
)