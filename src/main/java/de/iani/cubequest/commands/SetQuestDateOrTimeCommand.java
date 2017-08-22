package de.iani.cubequest.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.WaitForDateQuest;
import de.iani.cubequest.quests.WaitForTimeQuest;

public class SetQuestDateOrTimeCommand extends SubCommand {

    private boolean date;
    private SimpleDateFormat formatDay, formatTime;

    public SetQuestDateOrTimeCommand(boolean date) {
        this.date = date;
        if (date) {
            this.formatDay = new SimpleDateFormat("dd.MM.yyyy");
            this.formatTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(date? (quest instanceof WaitForDateQuest) : (quest instanceof WaitForTimeQuest))) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest erfordert kein" + (date? " Datum" : "e Zeit") + ".");
            return true;
        }

        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                CubeQuest.sendWarningMessage(sender, "Bitte gib " + (date? "das Datum" : "die Zeit") + " an.");
                return true;
            }
        }

        long res;
        if (date) {
            String dateString = args.getNext();
            try {
                res = formatTime.parse(dateString).getTime();
            } catch (ParseException e) {
                try {
                    res = formatDay.parse(dateString).getTime();
                } catch (ParseException f) {
                    CubeQuest.sendWarningMessage(sender, "Bitte gib das Datum im Format tt.mm.jjjj oder tt.mm.jjjj hh:mm:ss an.");
                    return true;
                }
            }
        } else {
            try {
                res = args.getAllTimespan();
            } catch (NumberFormatException | ParseException e) {
                CubeQuest.sendWarningMessage(sender, "Bitte gib die Zeitspanne in dem Format Wd Xh Ym Zs an, wobei W-Z ganze Zahlen sind (einzelne Blöcke des Formats können weggelassen werden).");
                return true;
            }
        }


        if (date) {
            ((WaitForDateQuest) quest).setDate(res);
            CubeQuest.sendNormalMessage(sender, "Datum auf den " + formatTime.format(date) + " Uhr gesetzt.");
        } else {
            ((WaitForTimeQuest) quest).setTime(res);
            long days = res / (1000*60*60*24);
            long hours = (res / (1000*60*60)) % (1000*60*60*24);
            long minutes = (res / (1000*60)) % (1000*60*60);
            long seconds = (res / 1000) % (1000*60);
            CubeQuest.sendNormalMessage(sender, "Zeit auf " + days + "d " + hours + "h " + minutes + "m " + seconds + "s gesetzt.");
        }


        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}