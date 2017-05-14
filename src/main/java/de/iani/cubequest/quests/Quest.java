package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.base.Verify;

import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldFailEvent;
import de.iani.cubequest.events.QuestWouldSucceedEvent;
import net.citizensnpcs.api.event.NPCClickEvent;

public abstract class Quest {

    //TODO: Server als Feld einfügen (da viele Sachen nur Serverweit eindeutig)

    private Integer id;
    private String name;
    private String giveMessage;
    private String successMessage;
    private String failMessage;
    private Reward successReward;
    private Reward failReward;
    private HashMap<UUID, Status> givenToPlayers;
    private boolean ready;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS, FAIL
    }

    public Quest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        Verify.verifyNotNull(name);

        this.name = name;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.failMessage = failMessage;
        this.successReward = successReward;
        this.failReward = failReward;
        this.givenToPlayers = new HashMap<UUID, Status>();
        this.ready = false;
    }

    public Quest(String name, String giveMessage, String successMessage, Reward successReward) {
        this(name, giveMessage, successMessage, null, successReward, null);
    }

    public Quest(String name) {
        this(name, null, null, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        if (this.id != null) {
            throw new IllegalStateException("Already has an id.");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        Verify.verifyNotNull(val);

        QuestRenameEvent event = new QuestRenameEvent(this, name, val);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.name = event.getNewName();
        }
    }

    public String getGiveMessage() {
        return giveMessage;
    }

    public void setGiveMessage(String giveMessage) {
        this.giveMessage = giveMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public Reward getSuccessReward() {
        return successReward;
    }

    public void setSuccessReward(Reward successReward) {
        this.successReward = successReward;
    }

    public Reward getFailReward() {
        return failReward;
    }

    public void setFailReward(Reward failReward) {
        this.failReward = failReward;
    }

    public void giveToPlayer(Player player) {
        if (!ready) {
            throw new IllegalStateException("Quest is not ready!");
        }
        if (giveMessage != null) {
            player.sendMessage(giveMessage);
        }
        givenToPlayers.put(player.getUniqueId(), Status.GIVENTO);
    }

    public void removeFromPlayer(UUID id) {
        givenToPlayers.remove(id);
    }

    public boolean onSuccess(Player player) {
        QuestWouldSucceedEvent event = new QuestWouldSucceedEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (successReward != null) {
            if (!successReward.pay(player)) {
                return false;
            }
        }

        Bukkit.getPluginManager().callEvent(new QuestSuccessEvent(this, player));

        if (successMessage != null) {
            player.sendMessage(successMessage);
        }
        givenToPlayers.put(player.getUniqueId(), Status.SUCCESS);
        return true;
    }

    public boolean onFail(Player player) {
        QuestWouldFailEvent event = new QuestWouldFailEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (failReward != null) {
            if (!failReward.pay(player)) {
                return false;
            }
        }

        Bukkit.getPluginManager().callEvent(new QuestFailEvent(this, player));

        if (failMessage != null) {
            player.sendMessage(failMessage);
        }
        givenToPlayers.put(player.getUniqueId(), Status.FAIL);
        return true;
    }

    public Status getPlayerStatus(UUID id) {
        return givenToPlayers.containsKey(id)? givenToPlayers.get(id) : Status.NOTGIVENTO;
    }

    /**
     *
     * @return HashSet (Kopie) mit allen UUIDs, deren Status GIVENTO ist.
     */
    public Collection<UUID> getPlayersGivenTo() {
        HashSet<UUID> result = new HashSet<UUID>();
        for (UUID id: givenToPlayers.keySet()) {
            if (givenToPlayers.get(id) == Status.GIVENTO) {
                result.add(id);
            }
        }
        return result;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean val) {
        if (val) {
            if (!isLegal()) {
                throw new IllegalStateException("Quest is not legal");
            }
            this.ready = true;
        } else if (this.ready && givenToPlayers.containsValue(Status.GIVENTO)) {
            throw new IllegalStateException("Already given to some players, can not be eddited!");
        }
        this.ready = false;
    }

    public abstract boolean isLegal();

    // Alle relevanten Block-Events

    public boolean onBlockBreakEvent(BlockBreakEvent event) {
        return true;
    }

    public boolean onBlockPlaceEvent(BlockPlaceEvent event) {
        return true;
    }

    // Alle relevanten Entity-Events

    public boolean onEntityDeathEvent(EntityDeathEvent event) {
        return true;
    }

    // Alle relevanten Player-Events

    public boolean onPlayerMoveEvent(PlayerMoveEvent event) {
        return true;
    }

    public boolean onPlayerFishEvent(PlayerFishEvent event) {
        return true;
    }

    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        return true;
    }

    // Alle relevanten NPC-Events

    public boolean onNPCClickEvent(NPCClickEvent event) {
        return true;
    }

    // Alle relevanten Quest-Events

    public boolean onQuestSuccessEvent(QuestSuccessEvent event) {
        return true;
    }

    public boolean onQuestFailEvent(QuestFailEvent event) {
        return true;
    }

}
