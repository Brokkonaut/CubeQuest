package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class BeInAreaCondition extends QuestCondition {
    
    private SafeLocation location;
    private double tolerance;
    
    public BeInAreaCondition(SafeLocation location, double tolerance) {
        this.location = Objects.requireNonNull(location);
        this.tolerance = tolerance;
        if (tolerance < 0) {
            throw new IllegalArgumentException("tolerance my not be negative");
        }
    }
    
    public BeInAreaCondition(Map<String, Object> serialized) {
        this((SafeLocation) serialized.get("location"),
                ((Number) serialized.get("tolerance")).doubleValue());
    }
    
    @Override
    public boolean fullfills(Player player, PlayerData data) {
        return this.location.isOnThisSever()
                && this.location.getLocation().distance(player.getLocation()) <= this.tolerance;
    }
    
    @Override
    public List<BaseComponent[]> getConditionInfo() {
        return Collections.singletonList(new ComponentBuilder(ChatColor.DARK_AQUA + "Im Gebiet: "
                + ChatAndTextUtil.getLocationInfo(this.location, this.tolerance)).create());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("location", this.location);
        result.put("tolerance", this.tolerance);
        return result;
    }
    
}
