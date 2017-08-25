package de.iani.cubequest.quests;

import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class ServerDependendQuest extends Quest {

    private int serverId;

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = serverId;
    }

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = CubeQuest.getInstance().getServerId();
    }

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, int serverId) {
        this(id, name, giveMessage, successMessage, null, successReward, null);
    }

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, Reward successReward) {
        this(id, name, giveMessage, successMessage, null, successReward, null);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        serverId = yc.getInt("serverId");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {

        yc.set("serverId", serverId);

        return super.serialize(yc);
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Server-ID: " + serverId + (isForThisServer()? ChatColor.GREEN + " (dieser Server)" : ChatColor.GOLD + " (ein anderer Server)")).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == serverId;
    }

    public int getServerId() {
        return serverId;
    }

    /**
     * Ruft KEIN QuestCreator#updateQuest auf! Sollte von aufrufender Quest getan werden!
     */
    protected void changeServerToThis() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }

}
