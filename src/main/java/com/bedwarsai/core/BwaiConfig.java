package com.bedwarsai.core;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class BwaiConfig {

    public static int maxBots = 8;
    public static boolean debugLogging = false;
    public static boolean giveStartKit = true;
    public static boolean botsFightBots = true;

    private static Configuration cfg;

    private BwaiConfig() {}

    public static void init(File file) {
        cfg = new Configuration(file);
        sync();
    }

    public static void sync() {
        maxBots = cfg.getInt("maxBots", "general", 8, 1, 16, "Numero maximo de bots simultaneos.");
        debugLogging = cfg.getBoolean("debugLogging", "general", false, "Mensagens extras no log.");
        giveStartKit = cfg.getBoolean("giveStartKit", "bots", true,
                "Da espada, la e macas douradas ao criar um bot.");
        botsFightBots = cfg.getBoolean("botsFightBots", "bots", true,
                "Bots atacam outros bots (sem equipes, todos sao inimigos).");
        if (cfg.hasChanged()) cfg.save();
    }
}
