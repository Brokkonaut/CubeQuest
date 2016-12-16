package de.iani.cubequest;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import de.iani.cubequest.quests.Quest;
import net.citizensnpcs.api.event.NPCClickEvent;

public class EventListener implements Listener {

    private CubeQuest plugin;

    public EventListener(CubeQuest plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onBlockBreakEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onBlockPlaceEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onEntityDeathEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNPCClickEvent(NPCClickEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onNPCClickEvent(event);
        }
    }

}
