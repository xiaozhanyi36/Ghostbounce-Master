/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.misc.sound

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSound : TipSoundPlayer
    var disableSound : TipSoundPlayer

    init {
        val enableSoundFile = File(LiquidBounce.fileManager.soundsDir, "enable.wav")
        val disableSoundFile = File(LiquidBounce.fileManager.soundsDir, "disable.wav")

        if (!enableSoundFile.exists())
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/liquidbounce+/sound/enable.wav")
        if (!disableSoundFile.exists())
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/liquidbounce+/sound/disable.wav")

        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
    }
}