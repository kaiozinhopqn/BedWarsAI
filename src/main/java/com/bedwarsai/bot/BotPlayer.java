package com.bedwarsai.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.WorldServer;

/** Jogador controlado pelo mod (EntityPlayerMP real, sem cliente). */
public class BotPlayer extends EntityPlayerMP {

    public BotPlayer(MinecraftServer server, WorldServer world, GameProfile profile) {
        super(server, world, profile, new ItemInWorldManager(world));
    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return false;
    }
}
