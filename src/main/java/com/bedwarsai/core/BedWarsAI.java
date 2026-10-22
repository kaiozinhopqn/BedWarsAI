package com.bedwarsai.core;

import com.bedwarsai.bot.BotManager;
import com.bedwarsai.command.BwaiCommand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = BedWarsAI.MODID,
     name = BedWarsAI.NAME,
     version = BedWarsAI.VERSION,
     acceptedMinecraftVersions = "[1.8.9]")
public class BedWarsAI {

    public static final String MODID = "bedwarsai";
    public static final String NAME = "BedWars AI";
    public static final String VERSION = "0.3.0";

    @Mod.Instance(MODID)
    public static BedWarsAI instance;

    public static Logger log;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        BwaiConfig.init(event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(new ServerEvents());
        log.info("{} {} carregado (maxBots={}).", NAME, VERSION, BwaiConfig.maxBots);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        BotManager.reset();
        event.registerServerCommand(new BwaiCommand());
    }

    /** Remove os bots ANTES do servidor desconectar jogadores (evita erro na conexao falsa). */
    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        BotManager.removeAll();
    }
}
