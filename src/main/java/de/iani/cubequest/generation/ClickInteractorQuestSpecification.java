package de.iani.cubequest.generation;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.quests.ClickInteractorQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.InvalidConfigurationException;

public class ClickInteractorQuestSpecification extends DifficultyQuestSpecification {
    
    private ClickInteractorQuest dataStorageQuest;
    
    public ClickInteractorQuestSpecification() {
        super();
        
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
        
        this.dataStorageQuest = new ClickInteractorQuest(-1);
    }
    
    public ClickInteractorQuestSpecification(Map<String, Object> serialized)
            throws InvalidConfigurationException {
        super(serialized);
        
        try {
            this.dataStorageQuest = (ClickInteractorQuest) serialized.get("dataStorageQuest");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    @Override
    public ClickInteractorQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated GotoQuest!", e);
            return null;
        }
        
        ClickInteractorQuest result = new ClickInteractorQuest(questId, questName, null,
                ChatColor.GOLD + getGiveMessage(), null, successReward, getInteractor());
        result.setDelayDatabseUpdate(true);
        result.setDisplayMessage(getGiveMessage());
        if (!(result.getInteractorName().equals(getInteractorName()))) {
            result.setInteractorName(getInteractorName());
        }
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabseUpdate(false);
        
        return result;
    }
    
    public Interactor getInteractor() {
        return this.dataStorageQuest.getInteractor();
    }
    
    public void setInteractor(Interactor interactor) {
        this.dataStorageQuest.setInteractor(interactor);
        update();
    }
    
    public String getInteractorName() {
        return this.dataStorageQuest.getInteractorName();
    }
    
    public void setInteractorName(String name) {
        this.dataStorageQuest.setInteractorName(name == null || name.equals("") ? null : name);
    }
    
    public String getGiveMessage() {
        return this.dataStorageQuest.getGiveMessage();
    }
    
    public void setGiveMessage(String giveMessage) {
        this.dataStorageQuest.setGiveMessage(giveMessage);
        update();
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new ComponentBuilder("").append(super.getSpecificationInfo())
                .append(ChatColor.DARK_AQUA + " Interactor: "
                        + ChatAndTextUtil
                                .getInteractorInfoString(this.dataStorageQuest.getInteractor()))
                .append(ChatColor.DARK_AQUA + " Name: " + getInteractorName())
                .append(ChatColor.DARK_AQUA + " Vergabenachricht: "
                        + (getGiveMessage() == null ? ChatColor.GOLD + "NULL"
                                : ChatColor.GREEN + getGiveMessage()))
                .create();
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compare(other);
        if (result != 0) {
            return result;
        }
        
        ClickInteractorQuestSpecification cnpcqs = (ClickInteractorQuestSpecification) other;
        
        int i1 = getInteractor() == null ? 0 : 1;
        int i2 = cnpcqs.getInteractor() == null ? 0 : 1;
        
        if (i1 != i2) {
            return i1 - i2;
        }
        
        return i1 == 0 ? 0 : getInteractor().compareTo(cnpcqs.getInteractor());
    }
    
    @Override
    public boolean isLegal() {
        return getInteractor() != null && getInteractor().isLegal() && getGiveMessage() != null;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", this.dataStorageQuest);
        return result;
    }
    
}