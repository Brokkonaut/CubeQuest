package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;

@DelegateDeserialization(Quest.class)
public class WaitForDateQuest extends Quest {
    
    private long dateInMs;
    private boolean done = false;
    private TimerTask task = null;
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            long dateInMs) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        this.dateInMs = dateInMs;
    }
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, long dateInMs) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                dateInMs);
    }
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            Date date) {
        this(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward, date.getTime());
    }
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Date date) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                date.getTime());
    }
    
    public WaitForDateQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.dateInMs = yc.getLong("dateInMs");
        
        checkTime();
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("dateInMs", this.dateInMs);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public void giveToPlayer(Player player) {
        super.giveToPlayer(player);
        
        if (System.currentTimeMillis() >= this.dateInMs) {
            onSuccess(player);
        }
    }
    
    @Override
    public boolean isLegal() {
        return this.dateInMs > 0;
    }
    
    @Override
    public void setReady(boolean val) {
        boolean before = isReady();
        super.setReady(val);
        
        if (before != isReady()) {
            checkTime();
        }
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Datum: " + ChatAndTextUtil.formatDate(this.dateInMs))
                        .create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        
        String waitedForDateString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + getStateStringStartingToken(state) + " " + ChatColor.GOLD + getName())
                            .create());
            waitedForDateString += Quest.INDENTION;
        } else {
            waitedForDateString += getStateStringStartingToken(state) + " ";
        }
        
        waitedForDateString += ChatColor.DARK_AQUA + "Auf den "
                + ChatAndTextUtil.formatDate(getDate()) + " gewartet: ";
        waitedForDateString +=
                state.getStatus().color + (state.getStatus() == Status.SUCCESS ? "ja" : "nein");
        
        result.add(new ComponentBuilder(waitedForDateString).create());
        
        return result;
    }
    
    @Override
    public boolean afterPlayerJoinEvent(QuestState state) {
        if (this.done) {
            onSuccess(state.getPlayerData().getPlayer());
            return true;
        }
        return false;
    }
    
    public void checkTime() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        Quest other = QuestManager.getInstance().getQuest(getId());
        if (other != this) {
            return;
        }
        if (!isReady()) {
            return;
        }
        if (System.currentTimeMillis() < this.dateInMs) {
            this.task = new TimerTask() {
                
                @Override
                public void run() {
                    Bukkit.getScheduler().runTask(CubeQuest.getInstance(), () -> checkTime());
                }
            };
            CubeQuest.getInstance().getTimer().schedule(this.task, getDate());
        } else {
            this.done = true;
            for (Player player: Bukkit.getOnlinePlayers()) {
                if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(getId())) {
                    onSuccess(player);
                }
            }
        }
    }
    
    public long getDateMs() {
        return this.dateInMs;
    }
    
    public Date getDate() {
        return new Date(this.dateInMs);
    }
    
    public void setDate(long ms) {
        if (this.done) {
            throw new IllegalStateException(
                    "WaitForDateQuest is already done and cannot be set to another date!");
        }
        
        this.dateInMs = ms;
        updateIfReal();
        checkTime();
    }
    
    public void setDate(Date date) {
        setDate(date.getTime());
    }
    
    public boolean isDone() {
        return this.done;
    }
    
    @Override
    public void onDeletion() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }
    
}
