package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.EntityTypesAndAmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatUtil;

public class ClearEntityTypesCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof EntityTypesAndAmountQuest)) {
            ChatUtil.sendWarningMessage(sender, "Diese Quest erfordert keine Materialien.");
            return true;
        }

        ((EntityTypesAndAmountQuest) quest).clearTypes();
        ChatUtil.sendNormalMessage(sender, "Alle EntityTypes für " + quest.getTypeName() + " [" + quest.getId() + "] " + " entfernt.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
