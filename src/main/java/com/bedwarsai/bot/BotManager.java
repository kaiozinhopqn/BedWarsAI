package com.bedwarsai.bot;

import com.bedwarsai.ai.BotBrain;
import com.bedwarsai.core.BedWarsAI;
import com.bedwarsai.core.BwaiConfig;
import com.bedwarsai.inventory.BotInventory;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Cria, ticka e remove bots. Tudo roda na thread do servidor. */
public final class BotManager {

    private static final Map<String, BotBrain> BRAINS = new LinkedHashMap<String, BotBrain>();

    private BotManager() {}

    public static Collection<BotBrain> all() {
        return new ArrayList<BotBrain>(BRAINS.values());
    }

    public static BotBrain get(String name) {
        return BRAINS.get(name.toLowerCase());
    }

    public static int count() {
        return BRAINS.size();
    }

    public static void reset() {
        BRAINS.clear();
    }

    /** Cria um bot perto do remetente. Retorna mensagem de erro, ou null se deu certo. */
    public static String add(ICommandSender sender, String name, Difficulty diff, Personality pers) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return "Servidor indisponivel.";
        if (!name.matches("[A-Za-z0-9_]{1,16}")) return "Nome invalido (use 1-16 letras, numeros ou _).";
        if (BRAINS.containsKey(name.toLowerCase())) return "Ja existe um bot com esse nome.";
        if (server.getConfigurationManager().getPlayerByUsername(name) != null) {
            return "Ja existe um jogador com esse nome.";
        }
        if (BRAINS.size() >= BwaiConfig.maxBots) {
            return "Limite de bots atingido (" + BwaiConfig.maxBots + "). Ajuste em config/bedwarsai.cfg.";
        }
        if (!(sender.getEntityWorld() instanceof WorldServer)) return "Mundo invalido.";
        WorldServer world = (WorldServer) sender.getEntityWorld();

        double x, y, z;
        float yaw = 0f;
        Entity e = sender.getCommandSenderEntity();
        if (e != null) {
            yaw = e.rotationYaw;
            double rad = Math.toRadians(yaw);
            x = e.posX - Math.sin(rad) * 2.0;
            z = e.posZ + Math.cos(rad) * 2.0;
            y = e.posY;
        } else {
            // console/comando: usa o ponto de spawn do mundo (0,0,0 ficaria dentro do terreno)
            BlockPos spawn = world.getSpawnPoint();
            x = spawn.getX() + 0.5;
            y = spawn.getY();
            z = spawn.getZ() + 0.5;
        }

        GameProfile profile = new GameProfile(
                UUID.nameUUIDFromBytes(("BedWarsAI-bot:" + name).getBytes(Charset.forName("UTF-8"))), name);
        BotPlayer bot = new BotPlayer(server, world, profile);
        new NetHandlerPlayServer(server, new FakeNetworkManager(), bot);
        bot.theItemInWorldManager.setGameType(WorldSettings.GameType.SURVIVAL);
        bot.setLocationAndAngles(x, y, z, yaw, 0f);
        bot.rotationYawHead = yaw;

        ServerConfigurationManager scm = server.getConfigurationManager();
        scm.playerEntityList.add(bot);
        scm.sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, bot));
        world.spawnEntityInWorld(bot);
        world.getPlayerManager().addPlayer(bot);

        if (BwaiConfig.giveStartKit) BotInventory.giveKit(bot);

        BRAINS.put(name.toLowerCase(), new BotBrain(bot, diff, pers));
        BedWarsAI.log.info("Bot '{}' criado ({}, {}).", name, diff, pers);
        return null;
    }

    public static boolean remove(String name) {
        BotBrain b = BRAINS.remove(name.toLowerCase());
        if (b == null) return false;
        despawn(b);
        return true;
    }

    public static int removeAll() {
        List<BotBrain> list = new ArrayList<BotBrain>(BRAINS.values());
        BRAINS.clear();
        for (BotBrain b : list) despawn(b);
        return list.size();
    }

    private static void despawn(BotBrain b) {
        try {
            MinecraftServer server = MinecraftServer.getServer();
            WorldServer world = (WorldServer) b.bot.worldObj;
            world.removeEntity(b.bot);
            world.getPlayerManager().removePlayer(b.bot);
            if (server != null) {
                ServerConfigurationManager scm = server.getConfigurationManager();
                scm.playerEntityList.remove(b.bot);
                scm.sendPacketToAllPlayers(
                        new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.REMOVE_PLAYER, b.bot));
            }
            BedWarsAI.log.info("Bot '{}' removido.", b.bot.getName());
        } catch (Throwable t) {
            BedWarsAI.log.error("Falha ao remover bot " + b.bot.getName(), t);
        }
    }

    /** Chamado no fim de cada tick do servidor. */
    public static void tick() {
        if (BRAINS.isEmpty()) return;
        Iterator<BotBrain> it = BRAINS.values().iterator();
        while (it.hasNext()) {
            BotBrain b = it.next();
            BotPlayer bot = (BotPlayer) b.bot;
            if (bot.isDead || bot.getHealth() <= 0f) {
                it.remove();
                despawn(b);
                continue;
            }
            try {
                double prevY = bot.posY;
                b.tick();
                bot.onUpdateEntity();
                bot.handleFalling(bot.posY - prevY, bot.onGround);
                ((WorldServer) bot.worldObj).getPlayerManager().updateMountedMovingPlayer(bot);
            } catch (Throwable t) {
                BedWarsAI.log.error("Erro ao atualizar bot " + bot.getName(), t);
            }
        }
    }
}
