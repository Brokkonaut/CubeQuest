package de.iani.cubequest.quests;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;

public class WaitForDateQuest extends Quest {

    private long dateInMs;
    private boolean done = false;

    public WaitForDateQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            long dateInMs) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);
        this.dateInMs = dateInMs;
    }

    public WaitForDateQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            long dateInMs) {
        this(id, name, giveMessage, successMessage, null, successReward, null, dateInMs);
    }

    public WaitForDateQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Date date) {
        this(id, name, giveMessage, successMessage, failMessage, successReward, failReward, date.getTime());
    }

    public WaitForDateQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Date date) {
        this(id, name, giveMessage, successMessage, null, successReward, null, date.getTime());
    }

    public WaitForDateQuest(int id) {
        this(id, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        this.dateInMs = yc.getLong("dateInMs");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("dateInMs", dateInMs);

        return yc.toString();
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> checkTime(), Math.max(1, (dateInMs-System.currentTimeMillis())*20/1000));
        }
    }

    private void checkTime() {
        Quest other = QuestManager.getInstance().getQuest(this.getId());
        if (other != this) {
            return;
        }
        if (!isReady()) {
            return;
        }
        if (System.currentTimeMillis() < dateInMs) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> checkTime(), Math.max(1, (dateInMs-System.currentTimeMillis())*20/1000));
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

    public void checkPlayer(Player player) {
        if (!done) {
            return;
        }
        if (!CubeQuest.getInstance().getPlayerData(player).isGivenTo(this.getId())) {
            return;
        }
        onSuccess(player);
    }

    public long getDateMs() {
        return dateInMs;
    }

    public Date getDate() {
        return new Date(dateInMs);
    }

    public void setDate(long ms) {
        this.dateInMs = ms;
    }

    public void setDate(Date date) {
        this.dateInMs = date.getTime();
    }

}