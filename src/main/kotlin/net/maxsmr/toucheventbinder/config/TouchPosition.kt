package net.maxsmr.toucheventbinder.config

import com.google.gson.annotations.SerializedName
import net.maxsmr.commonutils.data.Validable

data class TouchPosition(
        @SerializedName("x")
        val x: Int,
        @SerializedName("y")
        val y: Int
) : Validable {

    override fun isValid() = x >= 0 && y >= 0
}