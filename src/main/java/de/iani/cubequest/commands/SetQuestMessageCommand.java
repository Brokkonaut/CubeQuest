package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;

public class SetQuestMessageCommand extends SubCommand {

    private MessageTrigger when;

    public enum MessageTrigger {
        GIVE, SUCCESS, FAIL
    }

    public SetQuestMessageCommand(MessageTrigger when) {
        this.when = when;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        String msg = "";
        while (args.hasNext()) {
            msg += args.getNext() + " ";
        }
        msg = msg.equals("")? null : msg.substring(0, msg.length() - " ".length());

        switch (when) {
            case GIVE: quest.setGiveMessage(msg); break;
            case SUCCESS: quest.setSuccessMessage(msg); break;
            case FAIL: quest.setFailMessage(msg); break;
        }
        if (msg == null) {
            CubeQuest.sendNormalMessage(sender, when + "-Message für " + quest.getTypeName() + " [" + quest.getId() + "] gelöscht.");
        } else {
            CubeQuest.sendNormalMessage(sender, when + "-Message für " + quest.getTypeName() + " [" + quest.getId() + "] lautet jetzt:");
            sender.sendMessage(msg);
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}