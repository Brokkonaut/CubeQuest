package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CommandRouter implements CommandExecutor, TabCompleter {
    
    private class CommandMap {
        
        private String name;
        
        private CommandMap parent;
        
        private HashMap<String, CommandMap> subCommands;
        
        private ArrayList<CommandMap> subcommandsOrdered;
        
        private SubCommand executor;
        
        public CommandMap(CommandMap parent, String name) {
            this.parent = parent;
            this.name = name;
        }
    }
    
    private CommandMap commands;
    
    public CommandRouter(PluginCommand command) {
        this.commands = new CommandMap(null, null);
        command.setExecutor(this);
        command.setTabCompleter(this);
    }
    
    public void addPluginCommand(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }
    
    public void addCommandMapping(SubCommand command, String... route) {
        CommandMap current = this.commands;
        for (int i = 0; i < route.length; i++) {
            if (current.subCommands == null) {
                current.subCommands = new HashMap<>();
                current.subcommandsOrdered = new ArrayList<>();
            }
            String routePart = route[i].toLowerCase();
            CommandMap part = current.subCommands.get(routePart);
            if (part == null) {
                part = new CommandMap(current, routePart);
                current.subCommands.put(routePart, part);
                current.subcommandsOrdered.add(part);
            }
            current = part;
        }
        if (current.executor != null) {
            throw new IllegalArgumentException(
                    "Path " + Arrays.toString(route) + " is already mapped!");
        }
        current.executor = command;
    }
    
    public void addAlias(String alias, String... route) {
        if (route.length == 0) {
            throw new IllegalArgumentException("Route may not be empty!");
        }
        alias = alias.toLowerCase().trim();
        CommandMap current = this.commands;
        for (int i = 0; i < route.length - 1; i++) {
            if (current.subCommands == null) {
                throw new IllegalArgumentException(
                        "Path " + Arrays.toString(route) + " is not mapped!");
            }
            String routePart = route[i].toLowerCase();
            CommandMap part = current.subCommands.get(routePart);
            if (part == null) {
                throw new IllegalArgumentException(
                        "Path " + Arrays.toString(route) + " is not mapped!");
            }
            current = part;
        }
        CommandMap createAliasFor = current.subCommands.get(route[route.length - 1].toLowerCase());
        if (createAliasFor == null) {
            throw new IllegalArgumentException(
                    "Path " + Arrays.toString(route) + " is not mapped!");
        }
        if (current.subCommands.get(alias) != null) {
            route = route.clone();
            route[route.length - 1] = alias;
            throw new IllegalArgumentException(
                    "Path " + Arrays.toString(route) + " is already mapped!");
        }
        
        current.subCommands.put(alias, createAliasFor);
        // dont add to current.subcommandsOrdered, because it should not be shown in the
        // help
        // message
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            String[] args) {
        String partial = args.length > 0 ? args[args.length - 1] : "";
        CommandMap currentMap = this.commands;
        int nr = 0;
        while (currentMap != null) {
            String currentCmdPart = args.length - 1 > nr ? args[nr] : null;
            if (currentCmdPart != null) {
                currentCmdPart = currentCmdPart.toLowerCase();
            }
            // descend to subcommand?
            if (currentCmdPart != null && currentMap.subCommands != null) {
                CommandMap subMap = currentMap.subCommands.get(currentCmdPart);
                if (subMap != null) {
                    nr += 1;
                    currentMap = subMap;
                    continue;
                }
            }
            List<String> rv = null;
            // get tabcomplete options from command
            if (currentMap.executor != null) {
                rv = currentMap.executor.onTabComplete(sender, command, alias,
                        new ArgsParser(args, nr));
            }
            // get tabcomplete options from subcommands
            if (currentMap.subCommands != null) {
                if (rv == null) {
                    rv = new ArrayList<>();
                }
                for (Entry<String, CommandMap> entry : currentMap.subCommands.entrySet()) {
                    String key = entry.getKey();
                    if (StringUtil.startsWithIgnoreCase(key, partial)) {
                        CommandMap subcmd = entry.getValue();
                        if (subcmd.executor == null
                                || subcmd.executor.getRequiredPermission() == null
                                || sender.hasPermission(subcmd.executor.getRequiredPermission())) {
                            if (sender instanceof Player || subcmd.executor == null
                                    || !subcmd.executor.requiresPlayer()) {
                                if (subcmd.executor == null || subcmd.executor.isVisible()) {
                                    try {
                                        rv.add(key);
                                    } catch (UnsupportedOperationException e) {
                                        rv = new ArrayList<>(rv);
                                        rv.add(key);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return rv;
        }
        return null;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        return onCommand(sender, command, alias, 0, args);
    }
    
    public boolean onCommand(CommandSender sender, Command command, String alias,
            int argsPartOfAlias, String[] args) {
        try {
            boolean help = args.length > 0 && args[0].equalsIgnoreCase("help");
            
            CommandMap currentMap = this.commands;
            int nr = help ? 1 : 0;
            while (currentMap != null) {
                String currentCmdPart = args.length > nr ? args[nr] : null;
                if (currentCmdPart != null) {
                    currentCmdPart = currentCmdPart.toLowerCase();
                }
                // descend to subcommand?
                if (currentCmdPart != null && currentMap.subCommands != null) {
                    CommandMap subMap = currentMap.subCommands.get(currentCmdPart);
                    if (subMap != null) {
                        nr += 1;
                        currentMap = subMap;
                        continue;
                    }
                }
                // execute this?
                SubCommand toExecute = currentMap.executor;
                if (toExecute != null) {
                    if (toExecute.execute(sender, command, alias,
                            getCommandString(alias, argsPartOfAlias, currentMap),
                            new ArgsParser(args, nr))) {
                        return true;
                    }
                }
                
                // show valid cmds
                try {
                    if (args.length > nr) {
                        int page = Integer.parseInt(args[nr]) - 1;
                        showHelp(sender, alias, argsPartOfAlias, currentMap, page);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
                showHelp(sender, alias, argsPartOfAlias, currentMap);
                return true;
            }
            return false;
        } catch (Exception e) {
            
            ChatAndTextUtil.sendErrorMessage(sender,
                    "Beim Ausführen des Befehls ist ein interner Fehler aufgetreten.");
            
            if (sender instanceof Player) {
                CubeQuest.getInstance().getLogHandler().notifyPersonalLog((Player) sender);
                if (sender.hasPermission(CubeQuest.SEE_EXCEPTIONS_PERMISSION)) {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            ChatAndTextUtil.exceptionToString(e));
                }
            }
            
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Beim Ausführen eines CubeQuest-Command ist ein interner Fehler aufgetreten.",
                    e);
            return true;
        }
    }
    
    private String getCommandString(String alias, int argsPartOfAlias, CommandMap currentMap) {
        StringBuilder prefixBuilder = new StringBuilder();
        prefixBuilder.append('/').append(alias).append(' ');
        ArrayList<CommandMap> hierarchy = new ArrayList<>();
        CommandMap map = currentMap;
        while (map != null) {
            hierarchy.add(map);
            map = map.parent;
        }
        for (int i = hierarchy.size() - 2; i >= argsPartOfAlias; i--) {
            prefixBuilder.append(hierarchy.get(i).name).append(' ');
        }
        return prefixBuilder.toString();
    }
    
    private void showHelp(CommandSender sender, String alias, int argsPartOfAlias,
            CommandMap currentMap) {
        showHelp(sender, alias, argsPartOfAlias, currentMap, 0);
    }
    
    private void showHelp(CommandSender sender, String alias, int argsPartOfAlias,
            CommandMap currentMap, int page) {
        if (currentMap.subCommands == null) {
            return;
        }
        List<String> messages = new ArrayList<>();
        String prefix = getCommandString(alias, argsPartOfAlias, currentMap);
        for (CommandMap subcmd : currentMap.subcommandsOrdered) {
            String key = subcmd.name;
            if (subcmd.executor == null) {
                // hat weitere subcommands
                messages.add(prefix + key + " ...");
                continue;
            }
            if (subcmd.executor.getRequiredPermission() != null
                    && !sender.hasPermission(subcmd.executor.getRequiredPermission())) {
                continue;
            }
            if (subcmd.executor.requiresPlayer() && !(sender instanceof Player)) {
                continue;
            }
            if (!subcmd.executor.isVisible()) {
                continue;
            }
            
            messages.add(prefix + key + " " + subcmd.executor.getUsage());
        }
        
        Collections.sort(messages);
        String openPageCommandPrefix = prefix.replaceFirst(Pattern.quote(" "), " help ");
        ChatAndTextUtil.sendMessagesPaged(sender, ChatAndTextUtil.stringToSendableList(messages),
                page, "Command-Hilfe für " + prefix, openPageCommandPrefix);
    }
}
