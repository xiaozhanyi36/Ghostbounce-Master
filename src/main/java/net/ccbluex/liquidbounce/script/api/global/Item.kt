/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.script.api.global

import net.ccbluex.liquidbounce.utils.item.ItemCreator
import net.minecraft.item.ItemStack

/**
 * Object used by the script API to provide an easier way of creating items.
 */
object Item {

    /**
     * Creates an item.
     * @param itemArguments Arguments describing the item.
     * @return An instance of [ItemStack] with the given data.
     */
    @JvmStatic
    fun create(itemArguments: String): ItemStack = ItemCreator.createItem(itemArguments)
}