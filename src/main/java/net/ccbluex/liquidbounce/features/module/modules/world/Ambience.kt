/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S03PacketTimeUpdate

@ModuleInfo(name = "Ambience", description = "Change your world time and weather client-side.", category = ModuleCategory.WORLD)
class Ambience : Module() {
    val timeModeValue = ListValue("Time", arrayOf("Static", "Cycle"), "Static")
    val cycleSpeedValue = IntegerValue("CycleSpeed", 1, -24, 24, { timeModeValue.get().equals("cycle", true) })
    val staticTimeValue = IntegerValue("StaticTime", 18000, 0, 24000, { timeModeValue.get().equals("static", true) })
    val weatherModeValue = ListValue("Weather", arrayOf("Clear", "Rain", "NoModification"), "Clear")
    val rainStrengthValue = FloatValue("RainStrength", 0.1F, 0.01F, 1F, { weatherModeValue.get().equals("rain", true) })
    val tagValue = ListValue("Tag", arrayOf("TimeOnly", "Simplified", "Detailed", "None"), "TimeOnly")

    private var timeCycle = 0L

    override fun onEnable() {
        timeCycle = 0L
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S03PacketTimeUpdate)
            event.cancelEvent()
    }
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (timeModeValue.get().equals("static", true))
            mc.theWorld.setWorldTime(staticTimeValue.get().toLong())
        else {
            mc.theWorld.setWorldTime(timeCycle)
            timeCycle += (cycleSpeedValue.get() * 10).toLong()

            if (timeCycle > 24000L) timeCycle = 0L
            if (timeCycle < 0L) timeCycle = 24000L
        }

        if (!weatherModeValue.get().equals("nomodification", true))
            mc.theWorld.setRainStrength(if (weatherModeValue.get().equals("clear", true)) 0F else rainStrengthValue.get())
    }

    override val tag: String?
        get() = when (tagValue.get().toLowerCase()) {
            "timeonly" -> if (timeModeValue.get().equals("static", true)) staticTimeValue.get().toString() else timeCycle.toString()
            "simplified" -> "${if (timeModeValue.get().equals("static", true)) staticTimeValue.get().toString() else timeCycle.toString()}, ${weatherModeValue.get()}"
            "detailed" -> "Time: ${if (timeModeValue.get().equals("static", true)) staticTimeValue.get().toString() else "Cycle, ${timeCycle.toString()}"}, Weather: ${weatherModeValue.get()}"
            else -> null
        }
}