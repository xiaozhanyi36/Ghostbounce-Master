/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S1DPacketEntityEffect

@ModuleInfo(name = "AntiVanish", spacedName = "Anti Vanish", description = "Anti player vanish", category = ModuleCategory.WORLD)
class AntiVanish : Module() {
    private var lastNotify=-1L

    private val notifyLast = IntegerValue("Notification-Seconds", 2, 1, 30)

    @EventTarget
    fun onPacket(event: PacketEvent){
        if (mc.theWorld == null || mc.thePlayer == null) return
        if(event.packet is S1DPacketEntityEffect){
            if(mc.theWorld.getEntityByID(event.packet.entityId)==null){
                vanish()
            }
        }else if(event.packet is S14PacketEntity){
            if(event.packet.getEntity(mc.theWorld)==null){
                vanish()
            }
        }
    }

    private fun vanish(){
        if((System.currentTimeMillis()-lastNotify)>5000){
            LiquidBounce.hud.addNotification(Notification("AntiVanish","Found a vanished entity!", NotifyType.WARNING, notifyLast.get() * 1000))
        }
        lastNotify=System.currentTimeMillis()

    }
}