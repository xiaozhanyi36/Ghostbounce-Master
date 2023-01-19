package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.json.XMLTokener
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs


@ModuleInfo(name = "DamageParticle", spacedName = "Damage Particle", description = "Allows you to see targets damage.", category = ModuleCategory.RENDER)
class DamageParticle : Module() {

    var colorHeart = String()

    private val healthData = HashMap<Int, Float>()
    private val particles = ArrayList<SingleParticle>()

    private val aliveTicks = IntegerValue("AliveTicks", 20, 10, 50)
    private val sizeValue = IntegerValue("Size", 3, 1, 7)
    private val customColor = BoolValue("CustomColor", false)
    private val heartValue = BoolValue("Heart", false)
    private val outlineFont = BoolValue("OutlineFont", false)
    private val red = IntegerValue("Red", 255, 0, 255, { customColor.get() })
    private val green = IntegerValue("Green", 255, 0, 255, { customColor.get() })
    private val blue = IntegerValue("Blue", 255, 0, 255, { customColor.get() })

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(particles) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityLivingBase && EntityUtils.isSelected(entity, true)) {
                    val lastHealth = healthData.getOrDefault(entity.entityId, entity.maxHealth)
                    healthData[entity.entityId] = entity.health
                    if (lastHealth == entity.health) continue

                    val prefix = lastHealth > entity.health
                    val criticalHit = entity.hurtResistantTime < 18 || mc.thePlayer.motionY < 0 && !mc.thePlayer.onGround
                    val heart = lastHealth > entity.health
                    val colorHit = if (!customColor.get()) if (prefix) (if (criticalHit) "§c" else "§e") else "§a" else ""
                    if (heartValue.get()) {
                        colorHeart = if (!customColor.get()) if (heart) (if (criticalHit) "§c❤" else "§e❤") else "§a❤" else ""
                    } else {
                        colorHeart = ""
                    }
                    particles.add(SingleParticle(colorHit + BigDecimal(abs(lastHealth - entity.health).toDouble()).setScale(1, BigDecimal.ROUND_HALF_UP).toDouble() + colorHeart,
                        entity.posX - 0.5 + Random(System.currentTimeMillis()).nextInt(5).toDouble() * 0.1,
                        entity.entityBoundingBox.minY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) / 2.0,
                        entity.posZ - 0.5 + Random(System.currentTimeMillis() + 1L).nextInt(5).toDouble() * 0.1)
                    )
                }
            }

            val needRemove = ArrayList<SingleParticle>()
            for (particle in particles) {
                particle.ticks++
                if (particle.ticks>aliveTicks.get())
                    needRemove.add(particle)
            }

            for (particle in needRemove)
                particles.remove(particle)
        }
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        synchronized(particles) {
            val renderManager = mc.renderManager
            val size = sizeValue.get() * 0.01

            for (particle in particles) {
                val n = particle.posX - renderManager.renderPosX
                val n2 = particle.posY - renderManager.renderPosY
                val n3 = particle.posZ - renderManager.renderPosZ
                GlStateManager.pushMatrix()
                GlStateManager.enablePolygonOffset()
                GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
                GlStateManager.translate(n.toFloat(), n2.toFloat(), n3.toFloat())
                GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                val textY = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f

                GlStateManager.rotate(renderManager.playerViewX, textY, 0.0f, 0.0f)
                GlStateManager.scale(-size, -size, size)
                GL11.glDepthMask(false)
                if(outlineFont.get())
                    GameFontRenderer.drawOutlineStringWithoutGL(particle.str, (-(mc.fontRendererObj.getStringWidth(particle.str) / 2)).toFloat(), (-(mc.fontRendererObj.FONT_HEIGHT - 1)).toFloat(), if (customColor.get()) Color(red.get(), green.get(), blue.get()).rgb else 0, fontRenderer = mc.fontRendererObj)
                else
                    Fonts.minecraftFont.drawStringWithShadow(particle.str, (-(mc.fontRendererObj.getStringWidth(particle.str) / 2)).toFloat(), (-(mc.fontRendererObj.FONT_HEIGHT - 1)).toFloat(), if (customColor.get()) Color(red.get(), green.get(), blue.get()).rgb else 0)

                GL11.glColor4f(187.0f, 255.0f, 255.0f, 1.0f)
                GL11.glDepthMask(true)
                GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
                GlStateManager.disablePolygonOffset()
                GlStateManager.popMatrix()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        particles.clear()
        healthData.clear()
    }
}

class SingleParticle(val str: String, val posX: Double, val posY: Double, val posZ: Double) {
    var ticks = 0
}
