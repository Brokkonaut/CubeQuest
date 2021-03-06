package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class QuestWouldSucceedEvent extends QuestEvent implements Cancellable {
    
    private final Player player;
    private boolean cancelled = false;
    
    public QuestWouldSucceedEvent(Quest quest, Player player) {
        super(quest);
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }
    
}
