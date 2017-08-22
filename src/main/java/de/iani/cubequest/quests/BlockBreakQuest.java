package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;

public class BlockBreakQuest extends MaterialsAndAmountQuest {

    public BlockBreakQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, types, amount);
    }

    public BlockBreakQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public boolean onBlockBreakEvent(BlockBreakEvent event, QuestState state) {
        if (!getTypes().contains(event.getBlock().getType())) {
            return false;
        }
        AmountQuestState amountState = (AmountQuestState) state;
        if (amountState.getAmount()+1 >= getAmount()) {
            onSuccess(event.getPlayer());
        } else {
            amountState.changeAmount(1);
        }
        return true;
    }

}
