/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.world.AntiBot
import net.ccbluex.liquidbounce.utils.PathUtils

class PathfindingTeleportCommand : Command("ptp", emptyArray()) {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            val theName = args[1]

            // Get target player data
            val targetPlayer = mc.theWorld.playerEntities
                    .filter { !AntiBot.isBot(it) && it.name.equals(theName, true) }
                    .firstOrNull()

            // Attempt to teleport to player's position.
            if (targetPlayer != null) {
                val pathfinding = PathUtils.findBlinkPath(targetPlayer!!.posX, targetPlayer!!.posY, targetPlayer!!.posZ)
                for (path in pathfinding) 
                    mc.thePlayer.setPositionAndUpdate(path.x, path.y, path.z)
                chat("Attempted to teleport you to §a${targetPlayer!!.name}§3.")
                return
            } else {
                chat("§6We couldn't find any player in the current world with that name.")
                return
            }
        }
        else if (args.size == 4) {
            try {
                val posX = if (args[1].equals("~", true)) mc.thePlayer.posX else args[1].toDouble()
                val posY = if (args[2].equals("~", true)) mc.thePlayer.posY else args[2].toDouble()
                val posZ = if (args[3].equals("~", true)) mc.thePlayer.posZ else args[3].toDouble()

                val pathfinding = PathUtils.findBlinkPath(posX, posY, posZ)
                for (path in pathfinding) 
                    mc.thePlayer.setPositionAndUpdate(path.x, path.y, path.z)
                chat("Attempted to teleport you to §a$posX, $posY, $posZ§3.")
                return
            } catch (e: NumberFormatException) {
                chat("§6Please check if you have typed the numbers correctly, and try again.")
                return
            }
        }

        chatSyntax("teleport/tp <player name/x y z>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val pref = args[0]

        return when (args.size) {
            1 -> mc.theWorld.playerEntities
                    .filter { !AntiBot.isBot(it) && it.name.startsWith(pref, true) }
                    .map { it.name }
                    .toList()
            else -> emptyList()
        }
    }

}