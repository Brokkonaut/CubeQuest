package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

@DelegateDeserialization(Quest.class)
public class GotoQuest extends ServerDependendQuest {
    
    private String world;
    private double x, y, z;
    private double tolarance;
    
    private String overwrittenLocationName;
    
    public GotoQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, int serverId, String world, double x,
            double y, double z, double tolarance) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, serverId);
        
        if (isForThisServer() && world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) {
                throw new IllegalArgumentException("World " + w + " not found.");
            }
        }
        
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tolarance = tolarance;
    }
    
    public GotoQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Location location, double tolarance) {
        this(id, name, displayMessage, giveMessage, successMessage, successReward,
                CubeQuest.getInstance().getServerId(), location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), tolarance);
    }
    
    public GotoQuest(int id) {
        this(id, null, null, null, null, null, CubeQuest.getInstance().getServerId(), null, 0, 0, 0,
                0.5);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        String world = yc.getString("target.world");
        if (isForThisServer() && world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) {
                throw new IllegalArgumentException("World " + w + " not found.");
            }
        }
        
        this.world = world;
        this.x = yc.getDouble("target.x");
        this.y = yc.getDouble("target.y");
        this.z = yc.getDouble("target.z");
        this.tolarance = yc.getDouble("tolarance");
        this.overwrittenLocationName =
                yc.contains("overwrittenLocationName") ? yc.getString("overwrittenLocationName")
                        : null;
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        
        yc.createSection("target");
        yc.set("target.world", this.world);
        yc.set("target.x", this.x);
        yc.set("target.y", this.y);
        yc.set("target.z", this.z);
        yc.set("tolarance", this.tolarance);
        yc.set("overwrittenLocationName", this.overwrittenLocationName);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean onPlayerMoveEvent(PlayerMoveEvent event, QuestState state) {
        return checkForSuccess(event.getTo(), event.getPlayer());
    }
    
    private boolean checkForSuccess(Location loc, Player player) {
        if (!isForThisServer()) {
            return false;
        }
        if (!loc.getWorld().getName().equals(this.world)) {
            return false;
        }
        if (Math.abs(loc.getX() - this.x) > this.tolarance
                || Math.abs(loc.getY() - this.y) > this.tolarance
                || Math.abs(loc.getZ() - this.z) > this.tolarance) {
            return false;
        }
        if (!this.fullfillsProgressConditions(player,
                CubeQuest.getInstance().getPlayerData(player))) {
            return false;
        }
        
        onSuccess(player);
        return true;
    }
    
    @Override
    public void giveToPlayer(Player player) {
        super.giveToPlayer(player);
        checkForSuccess(player.getLocation(), player);
    }
    
    @Override
    public boolean isLegal() {
        return this.world != null && this.tolarance >= 0
                && (!isForThisServer() || Bukkit.getWorld(this.world) != null);
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Zu erreichender Ort: "
                + ChatAndTextUtil.getLocationInfo(this.world, this.x, this.y, this.z)).create());
        result.add(new ComponentBuilder(ChatAndTextUtil.getToleranceInfo(this.tolarance)).create());
        result.add(
                new ComponentBuilder(
                        ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + getLocationName() + " "
                                + (this.overwrittenLocationName == null
                                        ? ChatColor.GOLD + "(automatisch)"
                                        : ChatColor.GREEN + "(gesetzt)")).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        
        String goneToLocationString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + getStateStringStartingToken(state) + " " + ChatColor.GOLD + getName())
                            .create());
            goneToLocationString += Quest.INDENTION;
        } else {
            goneToLocationString += getStateStringStartingToken(state) + " ";
        }
        
        goneToLocationString +=
                ChatColor.DARK_AQUA + getLocationName() + ChatColor.DARK_AQUA + " erreicht: ";
        goneToLocationString +=
                state.getStatus().color + (state.getStatus() == Status.SUCCESS ? "ja" : "nein");
        
        result.add(new ComponentBuilder(goneToLocationString).create());
        
        return result;
    }
    
    public Location getTargetLocation() {
        return isForThisServer() && this.world != null
                ? new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z)
                : null;
    }
    
    public double getTargetX() {
        return this.x;
    }
    
    public double getTargetY() {
        return this.y;
    }
    
    public double getTargetZ() {
        return this.z;
    }
    
    public String getTargetWorld() {
        return this.world;
    }
    
    public void setLocation(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        if (!isForThisServer()) {
            changeServerToThis();
        }
        this.world = arg.getWorld().getName();
        this.x = arg.getX();
        this.y = arg.getY();
        this.z = arg.getZ();
        
        updateIfReal();
    }
    
    public double getTolarance() {
        return this.tolarance;
    }
    
    public void setTolarance(double arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("arg may not be negative");
        }
        this.tolarance = arg;
        updateIfReal();
    }
    
    public String getLocationName() {
        if (this.overwrittenLocationName != null) {
            return this.overwrittenLocationName;
        }
        
        String x, y, z;
        if (this.tolarance < 0.5) {
            x = Double.toString(this.x);
            y = Double.toString(this.y);
            z = Double.toString(this.z);
        } else {
            x = Integer.toString((int) Math.ceil(this.x));
            y = Integer.toString((int) Math.round(this.y));
            z = Integer.toString((int) Math.ceil(this.z));
        }
        
        String name = this.tolarance > 2 ? "ca. " : "";
        name += "x = " + x;
        name += ", y = " + y;
        name += ", z = " + z;
        if (this.tolarance < 0.5) {
            name += " ± " + this.tolarance;
        }
        name += " in Welt \"" + this.world + "\"";
        
        return name;
    }
    
    public void setLocationName(String name) {
        this.overwrittenLocationName = name;
        updateIfReal();
    }
    
}
