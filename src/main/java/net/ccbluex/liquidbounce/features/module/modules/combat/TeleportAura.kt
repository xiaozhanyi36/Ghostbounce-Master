/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * Parts of this code are based on InfiniteAura module from UnlegitMC/FDPClient.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PathUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "TeleportAura", spacedName = "Teleport Aura", description = "Automatically attacks targets around you. (by tp to them and back)",
        category = ModuleCategory.COMBAT)
class TeleportAura : Module() {

    /*
     * Values
     */
    private val apsValue = IntegerValue("APS", 1, 1, 10)
    private val maxTargetsValue = IntegerValue("MaxTargets", 2, 1, 8)
    private val rangeValue = IntegerValue("Range", 80, 10, 200, "m")
    private val fovValue = FloatValue("FOV", 180F, 0F, 180F, "°")
    private val maxMoveDistValue = FloatValue("MaxMoveSpeed", 8F, 2F, 15F, "m")
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val noPureC03Value = BoolValue("NoStandingPackets", true)
    private val noKillAuraValue = BoolValue("NoKillAura", true)
    private val renderValue = ListValue("Render", arrayOf("Box", "Lines", "None"), "Box")
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "LivingTime"), "Distance")

    /*
     * Variables
     */
    private val clickTimer = MSTimer()
    private var tpVectors = arrayListOf<Vec3>()
    private var thread: Thread? = null

    var lastTarget: EntityLivingBase? = null

    private lateinit var auraMod: KillAura

    private val attackDelay: Long
        get() = 1000L / apsValue.get().toLong()

    override val tag: String
        get() = "APS ${apsValue.get()}, Range ${rangeValue.get()}"

    override fun onEnable() {
        clickTimer.reset()
        tpVectors.clear()
        lastTarget = null
    }

    override fun onDisable() {
        clickTimer.reset()
        tpVectors.clear()
        lastTarget = null
    }

    override fun onInitialize() {
        auraMod = LiquidBounce.moduleManager.getModule(KillAura::class.java)!! as KillAura
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if ((noKillAuraValue.get() && auraMod.target != null) || !clickTimer.hasTimePassed(attackDelay)) return

        if (thread == null || !thread!!.isAlive) {
            tpVectors.clear()
            clickTimer.reset()
            thread = Thread { runAttack() }
            thread!!.start()
        } else
            clickTimer.reset()
    }

    private fun runAttack() {
        if ((noKillAuraValue.get() && auraMod.target != null) || mc.thePlayer == null || mc.theWorld == null) return

        val targets = arrayListOf<EntityLivingBase>()
        var entityCount = 0

        for (entity in mc.theWorld.loadedEntityList)
            if (entity is EntityLivingBase && EntityUtils.isSelected(entity, true) && mc.thePlayer.getDistanceToEntity(entity) <= rangeValue.get()) {
                if (fovValue.get() < 180F && RotationUtils.getRotationDifference(entity) > fovValue.get())
                    continue

                if (entityCount >= maxTargetsValue.get())
                    break

                targets.add(entity)
                entityCount++
            }

        if (targets.isEmpty()) {
            lastTarget = null
            return
        }

        // Sort targets by priority
        when (priorityValue.get().toLowerCase()) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntity(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
        }

        targets.forEach {
            if (mc.thePlayer == null || mc.theWorld == null) return

            val path = PathUtils.findTeleportPath(mc.thePlayer, it, maxMoveDistValue.get().toDouble())

            if (noKillAuraValue.get() && auraMod.target != null) return

            path.forEach { point -> 
                tpVectors.add(point)
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(point.xCoord, point.yCoord, point.zCoord, true)) 
            }

            lastTarget = it

            when (swingValue.get().toLowerCase()) {
                "normal" -> mc.thePlayer.swingItem()
                "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
            }

            mc.netHandler.addToSendQueue(C02PacketUseEntity(it, C02PacketUseEntity.Action.ATTACK))

            path.reversed().forEach { point -> 
                if (renderValue.get().equals("lines", true)) tpVectors.add(point)
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(point.xCoord, point.yCoord, point.zCoord, true)) 
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook)
            clickTimer.reset()

        if (noPureC03Value.get() && packet is C03PacketPlayer
            && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook)
            event.cancelEvent()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        synchronized (tpVectors) {
            if (renderValue.get().equals("none", true) || tpVectors.isEmpty()) return
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDepthMask(false)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

            GL11.glLoadIdentity()
            mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
            RenderUtils.glColor(Color.WHITE)
            GL11.glLineWidth(1F)

            if (renderValue.get().equals("lines", true))
                GL11.glBegin(GL11.GL_LINE_STRIP)

            try { 
                for (vec in tpVectors) {
                    val x = vec.xCoord - renderPosX
                    val y = vec.yCoord - renderPosY
                    val z = vec.zCoord - renderPosZ
                    val width = 0.3
                    val height = mc.thePlayer.getEyeHeight().toDouble()

                    when (renderValue.get().toLowerCase()) {
                        "box" -> {
                            GL11.glBegin(GL11.GL_LINE_STRIP)
                            GL11.glVertex3d(x - width, y, z - width)
                            GL11.glVertex3d(x - width, y, z - width)
                            GL11.glVertex3d(x - width, y + height, z - width)
                            GL11.glVertex3d(x + width, y + height, z - width)
                            GL11.glVertex3d(x + width, y, z - width)
                            GL11.glVertex3d(x - width, y, z - width)
                            GL11.glVertex3d(x - width, y, z + width)
                            GL11.glEnd()

                            GL11.glBegin(GL11.GL_LINE_STRIP)
                            GL11.glVertex3d(x + width, y, z + width)
                            GL11.glVertex3d(x + width, y + height, z + width)
                            GL11.glVertex3d(x - width, y + height, z + width)
                            GL11.glVertex3d(x - width, y, z + width)
                            GL11.glVertex3d(x + width, y, z + width)
                            GL11.glVertex3d(x + width, y, z - width)
                            GL11.glEnd()

                            GL11.glBegin(GL11.GL_LINE_STRIP)
                            GL11.glVertex3d(x + width, y + height, z + width)
                            GL11.glVertex3d(x + width, y + height, z - width)
                            GL11.glEnd()

                            GL11.glBegin(GL11.GL_LINE_STRIP)
                            GL11.glVertex3d(x - width, y + height, z + width)
                            GL11.glVertex3d(x - width, y + height, z - width)
                            GL11.glEnd()
                        }
                        "lines" -> GL11.glVertex3d(x, y, z)
                    }
                }
            } catch (e: Exception) {
                // ignore, concurrent modification error
            }

            if (renderValue.get().equals("lines", true))
                GL11.glEnd()

            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }
    }

}