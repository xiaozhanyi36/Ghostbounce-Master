/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

@ModuleInfo(name = "Criticals", description = "Automatically deals critical hits.", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf("NewPacket", "Packet", "Packet2", "NCPPacket", "NoGround", "Redesky", "AACv4", "Hop", "TPHop", "Jump", "Visual", "Edit", "MiniPhase", "NanoPacket", "Non-Calculable", "Invalid", "VerusSmart"), "Packet")
    val delayValue = IntegerValue("Delay", 0, 0, 500, "ms")
    private val jumpHeightValue = FloatValue("JumpHeight", 0.42F, 0.1F, 0.42F)
    private val downYValue = FloatValue("DownY", 0f, 0f, 0.1F)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val onlyAuraValue = BoolValue("OnlyAura", false)

    val msTimer = MSTimer()
    private var readyCrits = false
    private var canCrits = true
    private var counter = 0

    override fun onEnable() {
        if (modeValue.get().equals("NoGround", ignoreCase = true))
            mc.thePlayer.jump()
        canCrits = true
        counter = 0;
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (onlyAuraValue.get() && !LiquidBounce.moduleManager[KillAura::class.java]!!.state && !LiquidBounce.moduleManager[TeleportAura::class.java]!!.state) return

        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                    mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                    LiquidBounce.moduleManager[Fly::class.java]!!.state || !msTimer.hasTimePassed(delayValue.get().toLong()))
                return

            val x = mc.thePlayer.posX
            val y = mc.thePlayer.posY
            val z = mc.thePlayer.posZ

            when (modeValue.get().toLowerCase()) {
                "newpacket" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.05250000001304, z, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01400000001304, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    mc.thePlayer.onCriticalHit(entity)
                }

                "packet" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0625, z, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1.1E-5, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                    mc.thePlayer.onCriticalHit(entity)
                }

                "packet2" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0625, mc.thePlayer.posZ, false))
		            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.09858, mc.thePlayer.posZ, false))
		            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.04114514, mc.thePlayer.posZ, false))
		            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.025, mc.thePlayer.posZ, false))
                }

                "ncppacket" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.11, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.1100013579, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0000013579, z, false))
                    mc.thePlayer.onCriticalHit(entity)
                }

                "aacv4" -> {
                    mc.thePlayer.motionZ *= 0
                    mc.thePlayer.motionX *= 0
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3e-14, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8e-15, mc.thePlayer.posZ, true))
                }

                "hop" -> {
                    mc.thePlayer.motionY = 0.1
                    mc.thePlayer.fallDistance = 0.1f
                    mc.thePlayer.onGround = false
                }

                "tphop" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.02, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01, z, false))
                    mc.thePlayer.setPosition(x, y + 0.01, z)
                }

                "jump" -> {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = jumpHeightValue.get().toDouble()
                    } else {
                        mc.thePlayer.motionY -= downYValue.get()
                    }
                }

                "miniphase" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.0125, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01275, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.00025, z, true))
                }

                "nanopacket" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00973333333333, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.001, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.01200000000007, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.0005, z, false))
                }

                "non-calculable" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E-5, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E-7, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E-6, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E-4, z, false))
                }

                "invalid" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E+27, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E+68, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E+41, z, false))
                }

                "verussmart" -> {
                    counter++
                    if (counter == 1) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.001, z, true))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                    }
                    if (counter >= 5)
                        counter = 0
                }

                "visual" -> mc.thePlayer.onCriticalHit(entity)
            }

            readyCrits = true
            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (onlyAuraValue.get() && !LiquidBounce.moduleManager[KillAura::class.java]!!.state) return
        
        val packet = event.packet

        when (modeValue.get().toLowerCase()) {
            "redesky" -> {
                if (packet is C03PacketPlayer) {
                    val packetPlayer: C03PacketPlayer = packet as C03PacketPlayer
                    if(mc.thePlayer.onGround && canCrits) {
                        packetPlayer.y += 0.000001
                        packetPlayer.onGround = false
                    }
                    if(mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(
                                    0.0, (mc.thePlayer.motionY - 0.08) * 0.98, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                        packetPlayer.onGround = true;
                    }
                }
                if(packet is C07PacketPlayerDigging) {
                    if(packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        canCrits = false;
                    } else if(packet.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK || packet.status == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                        canCrits = true;
                    }
                }
            }

            "noground" -> {
                if (packet is C03PacketPlayer) {
                    packet.onGround = false
                }
            }

            "edit" -> {
                if (readyCrits) {
                    if (packet is C03PacketPlayer) {
                        packet.onGround = false
                    }
                    readyCrits = false
                }
            }
        }

    }

    override val tag: String?
        get() = modeValue.get()
}
