package de.iani.cubequest.quests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.iani.cubequest.CubeQuest;

public class CommandQuest extends Quest {

    private HashSet<String> commands;
    private HashSet<String[]> args;
    private boolean caseSensitive;

    /**
     * Erzeugt eine CommandQuest, bei der der Spieler einen bestimmten Befehl eingeben muss.
     * Der Befehl kann durch genaue Argumente spezifiziert werden.
     * @param name Name der Quest
     * @param giveMessage Nachricht, die der Spieler beim Start der Quest erhählt.
     * @param successMessage Nachricht, die der Spieler bei Abschluss der Quest erhählt.
     * @param successReward Belohnung, die der Spieler bei Abschluss der Quest erhählt.
     * @param commands Collection der Befehle, die der Spieler eingeben kann, um die Quest zu erfüllen.
     * @param args Collection von Argumenten, die der Spieler eingeben kann, um die Quest zu erfüllen. Null-Argumente sind immer erfüllt.
     * @param caseSensitive ob die Argumente case-senstitive sind (commands sind nie case-sensitive).
     */
    public CommandQuest(String name, String giveMessage, String successMessage, Reward successReward,
            Collection<String> commands, Collection<String[]> args, boolean caseSensitive) {
        super(name, giveMessage, successMessage, successReward);

        this.caseSensitive = caseSensitive;
        this.commands = new HashSet<String>();
        if (commands != null) {
            for (String s: commands) {
                if (s.startsWith("/")) {
                    s = s.substring(1);
                }
                commands.add(s.toLowerCase());
            }
        }
        this.args = new HashSet<String[]>();
        if (args != null) {
            for (String[] s: args) {
                this.args.add(Arrays.copyOf(s, s.length));
            }
        }
    }

    public CommandQuest(String name) {
        this(name, null, null, null, null, null, false);
    }

    @Override
    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (getPlayerStatus(event.getPlayer().getUniqueId()) != Status.GIVENTO) {
            return false;
        }
        String[] parts = event.getMessage().split(" ");
        if (parts.length < 1) {
            return false;
        }
        String cmd = parts[0].substring(1).toLowerCase();
        if (!commands.contains(cmd)) {
            return false;
        }
        String[] args = new String[parts.length - 1];
        for (int i=1; i<parts.length; i++) {
            args[i-1] = parts[i];
        }
        outerloop:
        for (String[] s: this.args) {
            for (int i=0; i<s.length; i++) {
                if (i >= args.length) {
                    continue outerloop;
                }
                if (caseSensitive) {
                    if (s[i] != null && !args[i].equals(s[i])) {
                        continue outerloop;
                    }
                } else {
                    if (s[i] != null && !args[i].equalsIgnoreCase(s[i])) {
                        continue outerloop;
                    }
                }
            }
            // onSuccess wird erst im nächsten Tick ausgelöst, damit der Befehl vorher durchlaufen kann
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                onSuccess(event.getPlayer());
            }, 1L);
            return true;
        }
        return false;
    }

    @Override
    public boolean isLegal() {
        return !commands.isEmpty();
    }

    public Set<String> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    public boolean addCommand(String cmd) {
        return commands.add(cmd.toLowerCase());
    }

    public boolean removeCommand(String cmd) {
        return commands.remove(cmd.toLowerCase());
    }

    public void clearCommands() {
        commands.clear();
    }

    public Set<String[]> getArgs() {
        return Collections.unmodifiableSet(args);
    }

    public boolean addArgs(String[] args) {
        args = Arrays.copyOf(args, args.length);
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("NULL") || args[i].equals("EGAL")) {
                args[i] = null;
            } else {
                args[i] = args[i].replaceAll("\\NULL", "NULL");
                args[i] = args[i].replaceAll("\\EGAL", "EGAL");
            }
        }
        return this.args.add(args);
    }

    public boolean removeArgs(String[] args) {
        boolean result = false;

        args = Arrays.copyOf(args, args.length);
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("NULL") || args[i].equals("EGAL")) {
                args[i] = null;
            } else {
                args[i] = args[i].replaceAll("\\NULL", "NULL");
                args[i] = args[i].replaceAll("\\EGAL", "EGAL");
            }
        }

        Iterator<String[]> argsIt = this.args.iterator();
        outerloop:
        while(argsIt.hasNext()) {
            String[] other = argsIt.next();
            if (other.length != args.length) {
                continue;
            }
            for (int i=0; i<other.length; i++) {
                if (caseSensitive) {
                    if (other[i] == null) {
                        if (args[i] != null) {
                            continue;
                        }
                    } else if (!other[i].equals(args[i])) {
                        continue outerloop;
                    }
                } else {
                    if (other[i] == null) {
                        if (args[i] != null) {
                            continue;
                        }
                    } else if (!other[i].equalsIgnoreCase(args[i])) {
                        continue outerloop;
                    }
                }
            }
            argsIt.remove();
            result = true;
        }
        return result;
    }

    public void clearArgs() {
        args.clear();
    }

}
