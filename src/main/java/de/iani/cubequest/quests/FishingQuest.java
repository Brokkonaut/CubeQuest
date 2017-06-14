package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState.Status;

public class FishingQuest extends Quest {

    private HashSet<Material> types;
    private int amount;

    public FishingQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward);
        Verify.verify(amount >= 0);

        if (types == null) {
            this.types = new HashSet<Material>();
        } else {
            this.types = new HashSet<Material>(types);
        }
        this.amount = amount;
    }

    public FishingQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        types.clear();
        List<String> typeList = yc.getStringList("types");
        for (String s: typeList) {
            types.add(Material.valueOf(s));
        }
        amount = yc.getInt("amount");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<String>();
        for (Material m: types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);
        yc.set("amount", amount);

        return super.serialize(yc);
    }

    @Override
    public boolean onPlayerFishEvent(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof Item)) {
            return false;
        }
        Item item = (Item) event.getCaught();
        if (!types.contains(item.getItemStack().getType())) {
            return false;
        }
        AmountQuestState state = (AmountQuestState) CubeQuest.getInstance().getPlayerData(event.getPlayer()).getPlayerState(this.getId());
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }
        if (state.getAmount()+1 >= amount) {
            onSuccess(event.getPlayer());
        } else {
            state.changeAmount(1);
        }
        return true;
    }

    @Override
    public boolean isLegal() {
        return !types.isEmpty() && amount > 0;
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), this.getId());
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int arg) {
        if (arg < 1) {
            throw new IllegalArgumentException("arg msut be greater than 0");
        }
        this.amount = arg;
    }

    public Set<Material> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public boolean addType(Material type) {
        return types.add(type);
    }

    public boolean removeType(Material type) {
        return types.remove(type);
    }

    public void clearTypes() {
        types.clear();
    }

}
