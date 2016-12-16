package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

public class ComplexQuest extends Quest {

    private Structure structure;
    private HashSet<Quest> partQuests;
    private Quest followupQuest;

    public enum Structure {
        ALLTOBEDONE, ONETOBEDONE
    }

    public ComplexQuest(String name, String giveMessage, String successMessage, Reward successReward,
            Structure structure, Collection<Quest> partQuests, Quest followupQuest) {
        super(name, giveMessage, successMessage, successReward);
        if (structure == null) throw new NullPointerException("structure may not be null");
        this.structure = structure;
        this.partQuests = new HashSet<Quest>(partQuests);
        this.followupQuest = followupQuest;

        for (Quest q: partQuests) {
            q.addSuperQuest(this);
        }
    }

    public Structure getStructure() {
        return structure;
    }

    /**
     * @return partQuests als unmodifiableCollection (live-Object, keine Kopie)
     */
    public Collection<Quest> getPartQuests() {
        return Collections.unmodifiableCollection(partQuests);
    }

    public Quest getFollowupQuest() {
        return followupQuest;
    }

    @Override
    public void giveToPlayer(Player player) {
        if (getPlayerStatus(player.getUniqueId()) != Status.NOTGIVENTO) return;
        super.giveToPlayer(player);
        for (Quest q: partQuests) {
            if (q.getPlayerStatus(player.getUniqueId()) == Status.NOTGIVENTO) q.giveToPlayer(player);
            else if (q.getPlayerStatus(player.getUniqueId()) == Status.SUCCESS) update(player);
        }
    }

    @Override
    public void onSuccess(Player player) {
        super.onSuccess(player);
        if (followupQuest != null) followupQuest.giveToPlayer(player);
    }

    @Override
    public void removeFromPlayer(UUID id) {
        if (getPlayerStatus(id) == Status.NOTGIVENTO) return;
        super.removeFromPlayer(id);
        for (Quest q: partQuests) {
            q.removeFromPlayer(id);
        }
    }

    public void update(Player player) {
        if (getPlayerStatus(player.getUniqueId()) != Status.GIVENTO) return;
        if (isSuccessfull(player.getUniqueId())) onSuccess(player);
    }

    private boolean isSuccessfull(UUID id) {
        switch(structure) {
            case ALLTOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) != Status.SUCCESS) return false;
                                }
                                return true;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) == Status.SUCCESS) return true;
                                }
                                return false;
            default: throw new IllegalStateException("Unknown Structure, should not happen!");  // Kompiliert nicht ohne default
        }
    }

}
