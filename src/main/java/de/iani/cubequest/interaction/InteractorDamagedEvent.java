package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public abstract class InteractorDamagedEvent<T extends Event & Cancellable> extends Event
        implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    protected final T original;
    private final Interactor interactor;
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public InteractorDamagedEvent(T original, Interactor interactor) {
        this.original = original;
        this.interactor = interactor;
    }
    
    public Interactor getInteractor() {
        return this.interactor;
    }
    
    public abstract Player getPlayer();
    
    public abstract String getNoPermissionMessage();
    
    @Override
    public boolean isCancelled() {
        return this.original.isCancelled();
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.original.setCancelled(cancel);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @Override
    public String toString() {
        return this.original.toString();
    }
    
}
