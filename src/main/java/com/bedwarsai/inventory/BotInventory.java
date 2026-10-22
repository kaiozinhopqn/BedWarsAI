package com.bedwarsai.inventory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/** Utilidades de hotbar (slots 0-8) para os bots. */
public final class BotInventory {

    private BotInventory() {}

    /** Kit inicial: espada, 2 pilhas de la e 3 macas douradas. */
    public static void giveKit(EntityPlayer bot) {
        bot.inventory.setInventorySlotContents(0, new ItemStack(Items.stone_sword));
        bot.inventory.setInventorySlotContents(1, new ItemStack(Blocks.wool, 64, 11));
        bot.inventory.setInventorySlotContents(2, new ItemStack(Blocks.wool, 64, 11));
        bot.inventory.setInventorySlotContents(3, new ItemStack(Items.golden_apple, 3));
        bot.inventory.currentItem = 0;
    }

    public static boolean isPlaceableBlock(ItemStack s) {
        if (s == null || s.stackSize <= 0 || !(s.getItem() instanceof ItemBlock)) return false;
        Block b = ((ItemBlock) s.getItem()).getBlock();
        return b.isFullCube() && !(b instanceof BlockFalling)
                && !(b instanceof BlockContainer) && b != Blocks.tnt;
    }

    public static int findBlockSlot(EntityPlayer p) {
        for (int i = 0; i < 9; i++) {
            if (isPlaceableBlock(p.inventory.mainInventory[i])) return i;
        }
        return -1;
    }

    public static int countBlocks(EntityPlayer p) {
        int n = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.inventory.mainInventory[i];
            if (isPlaceableBlock(s)) n += s.stackSize;
        }
        return n;
    }

    public static int findSword(EntityPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.inventory.mainInventory[i];
            if (s != null && s.getItem() instanceof ItemSword) return i;
        }
        return -1;
    }

    public static int findGoldenApple(EntityPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.inventory.mainInventory[i];
            if (s != null && s.stackSize > 0 && s.getItem() == Items.golden_apple) return i;
        }
        return -1;
    }

    public static void select(EntityPlayer p, int slot) {
        if (slot >= 0 && slot < 9) p.inventory.currentItem = slot;
    }
}
