/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.ScreenEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "KeepContainer", spacedName = "Keep Container", description = "Allows you to open a formerly closed inventory container everywhere. (Press INSERT Key to open)", category = ModuleCategory.WORLD)
class KeepContainer : Module() {

    private var container: GuiContainer? = null

    override fun onDisable() {
        if (container != null)
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow(container!!.inventorySlots.windowId))
        container = null
    }

    @EventTarget
    fun onGui(event: ScreenEvent) {
        val screen = event.guiScreen
        if (screen is GuiContainer && screen !is GuiInventory)
            container = screen
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        if (event.key == Keyboard.KEY_INSERT)
            mc.displayGuiScreen(container ?: return)
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0DPacketCloseWindow) 
            event.cancelEvent()
        if (packet is S2EPacketCloseWindow && container != null && container!!.inventorySlots != null && packet.windowId == container!!.inventorySlots.windowId)
            container = null
    }

}