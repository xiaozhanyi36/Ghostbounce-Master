/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import java.awt.Color

@ModuleInfo(name = "HUD", description = "Toggles visibility of the HUD.", category = ModuleCategory.RENDER, array = false)
class HUD : Module() {
    val tabHead = BoolValue("Tab-HeadOverlay", true)
    val animHotbarValue = BoolValue("AnimatedHotbar", true)
    val blackHotbarValue = BoolValue("BlackHotbar", true)
    val inventoryParticle = BoolValue("InventoryParticle", false)
    val fontChatValue = BoolValue("FontChat", false)
    val cmdBorderValue = BoolValue("CommandChatBorder", true)
    val fontType = FontValue("Font", Fonts.font40, { fontChatValue.get() })
    val chatRectValue = BoolValue("ChatRect", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
    val chatAnimationValue = BoolValue("ChatAnimation", true)
    val chatAnimationSpeedValue = FloatValue("Chat-AnimationSpeed", 0.1F, 0.01F, 0.1F)
    val Hudcolor = IntegerValue("OTC-Color", 121,106,229)


    private val toggleMessageValue = BoolValue("DisplayToggleMessage", false)
    private val toggleSoundValue = ListValue("ToggleSound", arrayOf("None", "Default", "Custom"), "Default")
    private val toggleVolumeValue = IntegerValue("ToggleVolume", 100, 0, 100, { toggleSoundValue.get().equals("custom", true) })
    val guiButtonStyle = ListValue("Button-Style", arrayOf("Minecraft", "LiquidBounce", "Rounded", "LiquidBounce+"), "Minecraft")

    val containerBackground = BoolValue("Container-Background", false)
    val containerButton = ListValue("Container-Button", arrayOf("TopLeft", "TopRight", "Off"), "TopLeft")
    val invEffectOffset = BoolValue("InvEffect-Offset", false)
    val domainValue = TextValue("Scoreboard-Domain", ".hud scoreboard-domain <your domain here>")
    private var hotBarX = 0F

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        LiquidBounce.hud.render(false)
    }

    @EventTarget(ignoreCondition = true)
    fun onTick(event: TickEvent) {
        if (LiquidBounce.moduleManager.shouldNotify != toggleMessageValue.get())
            LiquidBounce.moduleManager.shouldNotify = toggleMessageValue.get()

        if (LiquidBounce.moduleManager.toggleSoundMode != toggleSoundValue.values.indexOf(toggleSoundValue.get()))
            LiquidBounce.moduleManager.toggleSoundMode = toggleSoundValue.values.indexOf(toggleSoundValue.get())

        if (LiquidBounce.moduleManager.toggleVolume != toggleVolumeValue.get().toFloat())
            LiquidBounce.moduleManager.toggleVolume = toggleVolumeValue.get().toFloat()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        LiquidBounce.hud.update()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        LiquidBounce.hud.handleKey('a', event.key)
    }

    fun getAnimPos(pos: Float): Float {
        if (state && animHotbarValue.get()) hotBarX = AnimationUtils.animate(pos, hotBarX, 0.02F * RenderUtils.deltaTime.toFloat())
        else hotBarX = pos

        return hotBarX
    }

    init {
        state = true
    }
}