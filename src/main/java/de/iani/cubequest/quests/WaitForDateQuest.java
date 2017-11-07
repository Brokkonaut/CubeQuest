package de.iani.cubequest.quests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@DelegateDeserialization(Quest.class)
public class WaitForDateQuest extends Quest {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat(Util.DATE_AND_TIME_FORMAT_STRING);

    private long dateInMs;
    private boolean done = false;
    private int taskId = -1;

    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            long dateInMs) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward, failReward);
        this.dateInMs = dateInMs;
    }

    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            long dateInMs) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null, dateInMs);
    }

    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Date date) {
        this(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward, failReward, date.getTime());
    }

    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            Date date) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null, date.getTime());
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
        yc.set("dateInMs", dateInMs);

        return super.serializeToString(yc);
    }

    @Override
    public void giveToPlayer(Player player) {
        if (System.currentTimeMillis() > dateInMs) {
            throw new IllegalStateException("Date exceeded by " + (System.currentTimeMillis() - dateInMs) + " ms!");
        }
        super.giveToPlayer(player);
    }

    @Override
    public boolean isLegal() {
        return System.currentTimeMillis() < dateInMs;
    }

    @Override
    public boolean isReady() {
        return super.isReady() && !done;
    }

    @Override
    public void setReady(boolean val) {
        super.setReady(val);

        if (val) {
            taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> checkTime(), Math.max(1, (dateInMs-System.currentTimeMillis())*20/1000));
        } else if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Datum: " + dateFormat.format(new Date(dateInMs))).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    @Override
    public boolean afterPlayerJoinEvent(QuestState state) {
        if (done) {
            this.onSuccess(state.getPlayerData().getPlayer());
            return true;
        }
        return false;
    }

    public void checkTime() {
        if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        Quest other = QuestManager.getInstance().getQuest(this.getId());
        if (other != this) {
            return;
        }
        if (!isReady()) {
            return;
        }
        if (System.currentTimeMillis() < dateInMs) {
            taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> checkTime(), Math.max(1, (dateInMs-System.currentTimeMillis())*20/1000));
            return;
        } else {
            done = true;
            for (Player player: Bukkit.getOnlinePlayers()) {
                if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(this.getId())) {
                    this.onSuccess(player);
                }
            }
        }
    }

    public long getDateMs() {
        return dateInMs;
    }

    public Date getDate() {
        return new Date(dateInMs);
    }

    public void setDate(long ms) {
        this.dateInMs = ms;
        updateIfReal();
    }

    public void setDate(Date date) {
        setDate(date.getTime());
    }

}
