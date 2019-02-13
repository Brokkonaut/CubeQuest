package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;


public class RedstoneSignalAction extends QuestAction {
    
    private SafeLocation location;
    private long ticks;
    
    public RedstoneSignalAction(SafeLocation location, long ticks) {
        init(location, ticks);
    }
    
    public RedstoneSignalAction(Map<String, Object> serialized) {
        super(serialized);
        init((SafeLocation) serialized.get("location"),
                ((Number) serialized.get("ticks")).longValue());
    }
    
    private void init(SafeLocation location, long ticks) {
        this.location = Objects.requireNonNull(location).toBlockLocation();
        this.ticks = ticks;
        if (ticks <= 0) {
            throw new IllegalArgumentException("ticks must be positive");
        }
    }
    
    public SafeLocation getLocation() {
        return this.location;
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        BlockData potentialTarget = this.location.getLocation().getBlock().getBlockData();;
        if (!(potentialTarget instanceof Powerable)) {
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "No Powerable where RedstoneSignalAction should be performed: "
                            + getLocation());
            return;
        }
        
        Powerable target = (Powerable) potentialTarget;
        target.setPowered(true);
        this.location.getLocation().getBlock().setBlockData(Material.AIR.createBlockData());
        this.location.getLocation().getBlock().setBlockData(target);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            target.setPowered(false);
            this.location.getLocation().getBlock().setBlockData(target);
        }, this.ticks);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder(ChatColor.DARK_AQUA + "Redstone-Signal: "
                + ChatAndTextUtil.getLocationInfo(this.location) + ", " + this.ticks + " Ticks")
                        .create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("location", this.location);
        result.put("ticks", this.ticks);
        
        return result;
    }
    
}
