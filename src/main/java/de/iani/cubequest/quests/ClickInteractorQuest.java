package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.serialization.DelegateDeserialization;

@DelegateDeserialization(Quest.class)
public class ClickInteractorQuest extends InteractorQuest {
    
    public ClickInteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Interactor target) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, target);
    }
    
    public ClickInteractorQuest(int id) {
        this(id, null, null, null, null, null, null);
    }
    
    @Override
    public boolean playerConfirmedInteraction(QuestState state) {
        onSuccess(state.getPlayerData().getPlayer());
        return true;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        
        String interactorClickedString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + getStateStringStartingToken(state) + " " + ChatColor.GOLD + getName())
                            .create());
            interactorClickedString += Quest.INDENTION;
        } else {
            interactorClickedString += getStateStringStartingToken(state) + " ";
        }
        
        interactorClickedString +=
                ChatColor.DARK_AQUA + getInteractorName() + ChatColor.DARK_AQUA + " gefunden: ";
        interactorClickedString +=
                state.getStatus().color + (state.getStatus() == Status.SUCCESS ? "ja" : "nein");
        
        result.add(new ComponentBuilder(interactorClickedString).create());
        
        return result;
    }
    
}