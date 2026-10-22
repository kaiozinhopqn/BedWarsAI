package com.bedwarsai.command;

import com.bedwarsai.ai.BotBrain;
import com.bedwarsai.bot.BotManager;
import com.bedwarsai.bot.Difficulty;
import com.bedwarsai.bot.Personality;
import com.bedwarsai.core.BedWarsAI;
import com.bedwarsai.core.BwaiConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /bwai - apenas subcomandos que realmente existem nesta versao. */
public class BwaiCommand extends CommandBase {

    private static final List<String> ROOT = Arrays.asList("help", "version", "status", "bot");
    private static final List<String> BOT = Arrays.asList(
            "add", "remove", "removeall", "list", "difficulty", "personality");

    @Override
    public String getCommandName() { return "bwai"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/bwai help"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String sub = args.length == 0 ? "help" : args[0].toLowerCase();
        if (sub.equals("help")) {
            help(sender);
        } else if (sub.equals("version")) {
            send(sender, BedWarsAI.NAME + " v" + BedWarsAI.VERSION);
        } else if (sub.equals("status")) {
            send(sender, "Bots ativos: " + BotManager.count() + "/" + BwaiConfig.maxBots);
        } else if (sub.equals("bot")) {
            bot(sender, args);
        } else {
            err(sender, "Subcomando desconhecido: " + args[0] + ". Use /bwai help");
        }
    }

    private void help(ICommandSender s) {
        send(s, EnumChatFormatting.GOLD + "=== BedWars AI ===");
        send(s, "/bwai bot add <nome> [dificuldade] [personalidade]");
        send(s, "/bwai bot remove <nome>   |   /bwai bot removeall   |   /bwai bot list");
        send(s, "/bwai bot difficulty <nome> <EASY|NORMAL|HARD|INSANE>");
        send(s, "/bwai bot personality <nome> <AGGRESSIVE|BALANCED|DEFENSIVE>");
        send(s, "/bwai status   |   /bwai version");
        send(s, EnumChatFormatting.GRAY + "Pendentes: arenas, partidas BedWars, loja, interface grafica.");
    }

    private void bot(ICommandSender s, String[] a) {
        if (a.length < 2) { err(s, "Uso: /bwai bot <add|remove|removeall|list|difficulty|personality>"); return; }
        String act = a[1].toLowerCase();

        if (act.equals("add")) {
            if (a.length < 3) { err(s, "Uso: /bwai bot add <nome> [dificuldade] [personalidade]"); return; }
            Difficulty d = Difficulty.NORMAL;
            Personality p = Personality.BALANCED;
            if (a.length > 3) {
                d = Difficulty.parse(a[3]);
                if (d == null) { err(s, "Dificuldade invalida. Use EASY, NORMAL, HARD ou INSANE."); return; }
            }
            if (a.length > 4) {
                p = Personality.parse(a[4]);
                if (p == null) { err(s, "Personalidade invalida. Use AGGRESSIVE, BALANCED ou DEFENSIVE."); return; }
            }
            String e = BotManager.add(s, a[2], d, p);
            if (e != null) err(s, e); else send(s, "Bot " + a[2] + " criado (" + d + ", " + p + ").");
        } else if (act.equals("remove")) {
            if (a.length < 3) { err(s, "Uso: /bwai bot remove <nome>"); return; }
            if (BotManager.remove(a[2])) send(s, "Bot removido."); else err(s, "Bot nao encontrado.");
        } else if (act.equals("removeall")) {
            send(s, BotManager.removeAll() + " bot(s) removido(s).");
        } else if (act.equals("list")) {
            if (BotManager.count() == 0) { send(s, "Nenhum bot ativo."); return; }
            for (BotBrain b : BotManager.all()) send(s, b.describe());
        } else if (act.equals("difficulty")) {
            if (a.length < 4) { err(s, "Uso: /bwai bot difficulty <nome> <nivel>"); return; }
            BotBrain b = BotManager.get(a[2]);
            Difficulty d = Difficulty.parse(a[3]);
            if (b == null) { err(s, "Bot nao encontrado."); return; }
            if (d == null) { err(s, "Dificuldade invalida."); return; }
            b.difficulty = d;
            send(s, "Dificuldade de " + a[2] + " = " + d);
        } else if (act.equals("personality")) {
            if (a.length < 4) { err(s, "Uso: /bwai bot personality <nome> <personalidade>"); return; }
            BotBrain b = BotManager.get(a[2]);
            Personality p = Personality.parse(a[3]);
            if (b == null) { err(s, "Bot nao encontrado."); return; }
            if (p == null) { err(s, "Personalidade invalida."); return; }
            b.personality = p;
            send(s, "Personalidade de " + a[2] + " = " + p);
        } else {
            err(s, "Acao desconhecida: " + a[1]);
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, ROOT);
        if (args.length == 2 && args[0].equalsIgnoreCase("bot")) return getListOfStringsMatchingLastWord(args, BOT);
        if (args.length == 3 && args[0].equalsIgnoreCase("bot")) {
            String act = args[1].toLowerCase();
            if (act.equals("remove") || act.equals("difficulty") || act.equals("personality")) {
                List<String> names = new ArrayList<String>();
                for (BotBrain b : BotManager.all()) names.add(b.bot.getName());
                return getListOfStringsMatchingLastWord(args, names);
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("bot")) {
            String act = args[1].toLowerCase();
            if (act.equals("add") || act.equals("difficulty")) {
                return getListOfStringsMatchingLastWord(args, "EASY", "NORMAL", "HARD", "INSANE");
            }
            if (act.equals("personality")) {
                return getListOfStringsMatchingLastWord(args, "AGGRESSIVE", "BALANCED", "DEFENSIVE");
            }
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("bot") && args[1].equalsIgnoreCase("add")) {
            return getListOfStringsMatchingLastWord(args, "AGGRESSIVE", "BALANCED", "DEFENSIVE");
        }
        return null;
    }

    private static void send(ICommandSender s, String m) { s.addChatMessage(new ChatComponentText(m)); }

    private static void err(ICommandSender s, String m) {
        s.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + m));
    }
}
