package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowLevelCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Player player = (Player) sender;
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        
        int level = data.getLevel();
        int xp = data.getXp();
        int requiredXp = PlayerData.getXpRequiredForLevel(level + 1);
        
        ChatAndTextUtil.sendNormalMessage(sender,
                "Du hast " + xp + " Quest-XP und hast damit Level " + level + ".");
        ChatAndTextUtil.sendNormalMessage(sender,
                "Dir fehlen noch " + (requiredXp - xp) + " Quest-XP zum nächsten Level.");
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
}