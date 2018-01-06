package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;

public class RemoveQuestSpecificationCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Index der Spezifikation an, die du entfernen möchtest.");
            return true;
        }
        
        int index = args.getNext(-1);
        if (index < 1) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Der Index muss eine ganze Zahl echt größer als 0 sein.");
            return true;
        }
        
        if (index - 1 >= QuestGenerator.getInstance().getPossibleQuestsIncludingNulls().size()
                || QuestGenerator.getInstance().getPossibleQuestsIncludingNulls()
                        .get(index - 1) == null) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "An diesem Index gibt es keine Spezifikation.");
        }
        
        QuestGenerator.getInstance().removePossibleQuest(index - 1);
        ChatAndTextUtil.sendNormalMessage(sender, "Spezifikation entfernt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
}
