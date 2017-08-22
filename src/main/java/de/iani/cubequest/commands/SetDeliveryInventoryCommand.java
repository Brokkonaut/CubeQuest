package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.quests.Quest;

public class SetDeliveryInventoryCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        Player player = (Player) sender;    // sicher wegen requiresPlayer returns true
        ItemStack[] delivery = player.getInventory().getContents();
        ((DeliveryQuest) quest).setDelivery(delivery);

        CubeQuest.sendNormalMessage(sender, "Lieferumfang gesetzt.");
        return true;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}