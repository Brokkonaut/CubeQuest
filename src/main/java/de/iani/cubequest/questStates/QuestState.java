package de.iani.cubequest.questStates;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestState {
    
    private Status status;
    private PlayerData data;
    private Quest quest;
    
    public enum Status {
        NOTGIVENTO(ChatColor.GOLD, false),
        GIVENTO(ChatColor.GOLD, true),
        SUCCESS(ChatColor.GREEN, true),
        FAIL(ChatColor.RED, false),
        FROZEN(ChatColor.AQUA, false);
        
        private static Status[] values = values();
        
        static {
            NOTGIVENTO.invert = GIVENTO;
            GIVENTO.invert = NOTGIVENTO;
            SUCCESS.invert = FAIL;
            FAIL.invert = SUCCESS;
            FROZEN.invert = FROZEN;
        }
        
        public final ChatColor color;
        public final boolean succeedable;
        private Status invert;
        
        public static Status fromOrdinal(int ordinal) {
            return values[ordinal];
        }
        
        private Status(ChatColor color, boolean succeedable) {
            this.color = color;
            this.succeedable = succeedable;
        }
        
        public Status invert() {
            return this.invert;
        }
        
    }
    
    public QuestState(PlayerData data, int questId, Status status) {
        this.status = status == null ? Status.NOTGIVENTO : status;
        this.data = data;
        this.quest = QuestManager.getInstance().getQuest(questId);
        if (this.quest == null) {
            throw new IllegalArgumentException("No quest for this questId");
        }
    }
    
    public QuestState(PlayerData data, int questId) {
        this(data, questId, null);
    }
    
    protected void updated() {
        this.data.stateChanged(this);
    }
    
    public void invalidate() {
        
    }
    
    public Status getStatus() {
        return this.status;
    }
    
    public void setStatus(Status status, boolean updatePlayerData) {
        if (status == null) {
            throw new NullPointerException();
        }
        this.status = status;
        if (updatePlayerData) {
            updated();
        }
    }
    
    public void setStatus(Status status) {
        setStatus(status, true);
    }
    
    public PlayerData getPlayerData() {
        return this.data;
    }
    
    public Quest getQuest() {
        return this.quest;
    }
    
    /**
     * Erzeugt eine neue YamlConfiguration aus dem String und ruft dann
     * {@link Quest#deserialize(YamlConfigration)} auf.
     * 
     * @param serialized serialisierter Zustand
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public void deserialize(String serialized, Status status) throws InvalidConfigurationException {
        if (this.getClass() == QuestState.class && serialized.equals("")) {
            this.status = status == null ? Status.NOTGIVENTO : status;
            return;
        }
        YamlConfiguration yc = new YamlConfiguration();
        yc.loadFromString(serialized);
        deserialize(yc, status);
    }
    
    /**
     * Wendet den Inhalt der YamlConfiguration auf die Quest an.
     * 
     * @param yc serialisierte Zustands-Daten
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public void deserialize(YamlConfiguration yc, Status status)
            throws InvalidConfigurationException {
        if (!yc.getString("type")
                .equals(QuestStateType.getQuestStateType(this.getClass()).toString())) {
            throw new IllegalArgumentException("Serialized type doesn't match!");
        }
        this.status = status == null ? Status.NOTGIVENTO : status;
    }
    
    /**
     * Serialisiert den QuestState
     * 
     * @return serialisierter Zustand
     */
    public String serialize() {
        return (this.getClass() == QuestState.class) ? "" : serialize(new YamlConfiguration());
    }
    
    /**
     * Unterklassen sollten ihre Daten in die YamlConfiguration eintragen und dann die Methode der
     * Oberklasse aufrufen.
     * 
     * @param yc YamlConfiguration mit den Daten des QuestStates
     * @return serialisierter Zustand
     */
    protected String serialize(YamlConfiguration yc) {
        yc.set("type", QuestStateType.getQuestStateType(this.getClass()).toString());
        
        return yc.saveToString();
    }
    
    @Override
    public int hashCode() {
        return (this.status.ordinal() + 1) * (this.data.getId().hashCode() + this.quest.getId());
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof QuestState)) {
            return false;
        }
        
        QuestState state = (QuestState) other;
        return this.status == state.status && this.quest.equals(state.quest)
                && this.data.getId().equals(state.data.getId());
    }
    
}
