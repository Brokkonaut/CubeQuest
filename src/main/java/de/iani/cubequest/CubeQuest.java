package de.iani.cubequest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.iani.cubequest.commands.AddOrRemoveEntityTypeCommand;
import de.iani.cubequest.commands.AddOrRemoveMaterialCommand;
import de.iani.cubequest.commands.AddOrRemoveSubQuestCommand;
import de.iani.cubequest.commands.ClearEntityTypesCommand;
import de.iani.cubequest.commands.ClearMaterialsCommand;
import de.iani.cubequest.commands.ClearSubQuestsCommand;
import de.iani.cubequest.commands.CommandRouter;
import de.iani.cubequest.commands.CreateQuestCommand;
import de.iani.cubequest.commands.EditQuestCommand;
import de.iani.cubequest.commands.QuestEditor;
import de.iani.cubequest.commands.SetOrRemoveFollowupQuestCommand;
import de.iani.cubequest.commands.SetQuestAmountCommand;
import de.iani.cubequest.commands.SetQuestMessageCommand;
import de.iani.cubequest.commands.SetQuestMessageCommand.MessageTrigger;
import de.iani.cubequest.commands.SetQuestNameCommand;
import de.iani.cubequest.commands.SetQuestRegexCommand;
import de.iani.cubequest.commands.SetRewardCubesCommand;
import de.iani.cubequest.commands.SetRewardInventoryCommand;
import de.iani.cubequest.commands.StopEditingQuestCommand;
import de.iani.cubequest.commands.ToggleGenerateDailyQuestsCommand;
import de.iani.cubequest.commands.TogglePayRewardsCommand;
import de.iani.cubequest.commands.ToggleReadyStatusCommand;
import de.iani.cubequest.sql.DatabaseFassade;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.treasurechest.TreasureChest;
import de.iani.treasurechest.TreasureChestAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class CubeQuest extends JavaPlugin {

    public static final String PLUGIN_TAG = ChatColor.BLUE + "[CubeQuest]";
    public static final String EDIT_QUESTS_PERMISSION = "cubequest.admin";
    public static final String TOGGLE_SERVER_PROPERTIES_PERMISSION = "cubequest.admin";

    private static CubeQuest instance = null;

    private CommandRouter commandExecutor;
    private QuestCreator questCreator;
    private QuestStateCreator questStateCreator;
    private QuestEditor questEditor;
    private SQLConfig sqlConfig;
    private DatabaseFassade dbf;
    private NPCRegistry npcReg;

    private int serverId;
    private String serverName;
    private boolean generateDailyQuests;
    private boolean payRewards;

    private ArrayList<Runnable> waitingForPlayer;
    private Integer tickTask;
    private long tick = 0;

    private HashMap<UUID, PlayerData> playerData;

    public static void sendNormalMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(PLUGIN_TAG + " " + ChatColor.GREEN + msg);
    }

    public static void sendWarningMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(PLUGIN_TAG + " " + ChatColor.GOLD + msg);
    }

    public static void sendErrorMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(PLUGIN_TAG + " " + ChatColor.RED + msg);
    }

    public static void sendMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(PLUGIN_TAG + " " + msg);
    }

    public static void sendNoPermissionMessage(CommandSender recipient) {
        sendErrorMessage(recipient, "Dazu fehlt dir die Berechtigung!");
    }

    @SuppressWarnings("deprecation")
    public static EntityType matchEnum(String from) {
        EntityType res = EntityType.valueOf(from.toUpperCase(Locale.ENGLISH));
        if (res != null) {
            return res;
        }
        res = EntityType.fromName(from);
        if (res != null) {
            return res;
        }
        try {
            return EntityType.fromId(Integer.parseInt(from));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static CubeQuest getInstance() {
        if (instance == null) {
            instance = CubeQuest.getPlugin(CubeQuest.class);
        }
        return instance;
    }

    public static String capitalize(String s, boolean replaceUnderscores) {
        char[] cap = s.toCharArray();
        boolean lastSpace = true;
        for (int i = 0; i < cap.length; i++) {
            if (cap[i] == '_') {
                if (replaceUnderscores) {
                    cap[i] = ' ';
                }
                lastSpace = true;
            } else if (cap[i] >= '0' && cap[i] <= '9') {
                lastSpace = true;
            } else {
                if (lastSpace) {
                    cap[i] = Character.toUpperCase(cap[i]);
                } else {
                    cap[i] = Character.toLowerCase(cap[i]);
                }
                lastSpace = false;
            }
        }
        return new String(cap);
    }

    public CubeQuest() {
        this.playerData = new HashMap<UUID, PlayerData>();
        this.questCreator = new QuestCreator();
        this.questStateCreator = new QuestStateCreator();
        this.questEditor = new QuestEditor();
        this.waitingForPlayer = new ArrayList<Runnable>();
    }

    @Override
    public void onEnable() {
        sqlConfig = new SQLConfig(getConfig().getConfigurationSection("database"));

        dbf = new DatabaseFassade(this);
        if (!dbf.reconnect()) {
            return;
        }

        this.generateDailyQuests = this.getConfig().getBoolean("generateDailyQuests");
        this.payRewards = this.getConfig().getBoolean("payRewards");

        EventListener listener = new EventListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", listener);
        commandExecutor = new CommandRouter(getCommand("quest"));
        commandExecutor.addCommandMapping(new CreateQuestCommand(), "create");
        commandExecutor.addCommandMapping(new EditQuestCommand(), "edit");
        commandExecutor.addCommandMapping(new StopEditingQuestCommand(), "edit", "stop");
        commandExecutor.addCommandMapping(new ToggleReadyStatusCommand(), "setReady");
        commandExecutor.addCommandMapping(new SetQuestNameCommand(), "setName");
        commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.GIVE), "setGiveMessage");
        commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.SUCCESS), "setSuccessMessage");
        commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.FAIL), "setFailMessage");
        commandExecutor.addCommandMapping(new SetRewardInventoryCommand(true), "setSuccessRewardItems");
        commandExecutor.addCommandMapping(new SetRewardInventoryCommand(false), "setFailRewardItems");
        commandExecutor.addCommandMapping(new SetRewardCubesCommand(true), "setSuccessRewardCubes");
        commandExecutor.addCommandMapping(new SetRewardCubesCommand(false), "setFailRewardCubes");
        commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(true), "addSubQuest");
        commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(false), "removeSubQuest");
        commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(true), "setFollowupQuest");
        commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(false), "removeFollowupQuest");
        commandExecutor.addCommandMapping(new ClearSubQuestsCommand(), "clearSubQuests");
        commandExecutor.addCommandMapping(new SetQuestAmountCommand(), "setAmount");
        commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(true), "addMaterial");
        commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(false), "removeMaterial");
        commandExecutor.addCommandMapping(new ClearMaterialsCommand(), "clearMaterials");
        commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(true), "addEntityType");
        commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(false), "removeEntityType");
        commandExecutor.addCommandMapping(new ClearEntityTypesCommand(), "clearEntityTypes");
        commandExecutor.addCommandMapping(new SetQuestRegexCommand(true), "setLiteralMatch");
        commandExecutor.addCommandMapping(new SetQuestRegexCommand(false), "setRegex");
        commandExecutor.addCommandMapping(new TogglePayRewardsCommand(), "setPayRewards");
        commandExecutor.addCommandMapping(new ToggleGenerateDailyQuestsCommand(), "setGenerateDailyQuests");

        loadNPCs();
        loadServerIdAndName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            loadQuests();
        }, 1L);

        tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            tick();
        }, 2L, 1L);
    }

    private void loadNPCs() {
        /*npcReg = CitizensAPI.getNamedNPCRegistry("CubeQuestNPCReg");
        if (npcReg == null) {
            npcReg = CitizensAPI.createNamedNPCRegistry("CubeQuestNPCReg", SimpleNPCDataStore.create(new YamlStorage(
                    new File(this.getDataFolder().getPath() + File.separator + "npcs.yml"))));
        }*/
        npcReg = CitizensAPI.getNPCRegistry();
    }

    private void loadServerIdAndName() {
        if (getConfig().contains("serverId")) {
            serverId = getConfig().getInt("serverId");
        } else {
            try {
                serverId = dbf.addServerId();

                getConfig().set("serverId", serverId);
                this.saveConfig();
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Could not create serverId!", e);
            }
        }
        if (getConfig().contains("serverName")) {
            serverName = getConfig().getString("serverName");
        } else {
            waitingForPlayer.add(() -> {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("GetServers");
                    Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                    player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
                }, 1L);
            });
        }
    }

    private void loadQuests() {
        questCreator.loadQuests();
    }

    @Override
    public void onDisable() {
        if (tickTask != null && (Bukkit.getScheduler().isQueued(tickTask) || Bukkit.getScheduler().isCurrentlyRunning(tickTask))) {
            Bukkit.getScheduler().cancelTask(tickTask);
        }
    }

    private void tick() {
        tick ++;
        // Nothing, yet...
    }

    public QuestManager getQuestManager() {
        return QuestManager.getInstance();
    }

    public CommandRouter getCommandExecutor() {
        return commandExecutor;
    }

    public QuestCreator getQuestCreator() {
        return questCreator;
    }

    public QuestStateCreator getQuestStateCreator() {
        return questStateCreator;
    }

    public QuestEditor getQuestEditor() {
        return questEditor;
    }

    public NPCRegistry getNPCReg() {
        return npcReg;
    }

    public boolean isGeneratingDailyQuests() {
        return generateDailyQuests;
    }

    public void setGenerateDailyQuests(boolean val) {
        this.generateDailyQuests = val;
        this.getConfig().set("generateDailyQuests", val);
        this.saveConfig();
    }

    public boolean isPayRewards() {
        return payRewards;
    }

    public void setPayRewards(boolean val) {
        this.payRewards = val;
        this.getConfig().set("payRewards", val);
        this.saveConfig();
    }

    public int getServerId() {
        return serverId;
    }

    public String getBungeeServerName() {
        return serverName;
    }

    public void setBungeeServerName(String val) {
        serverName = val;
        try {
            dbf.setServerName();

            getConfig().set("serverName", serverName);
            getDataFolder().mkdirs();
            File configFile = new File(getDataFolder(), "config.yml");
            configFile.createNewFile();
            getConfig().save(configFile);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not set servername!", e);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config!", e);
        }
    }

    public String[] getOtherBungeeServers() {
        try {
            return dbf.getOtherBungeeServerNames();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not get servernames!", e);
            return new String[0];
        }
    }

    public boolean isWaitingForPlayer() {
        return !waitingForPlayer.isEmpty();
    }

    public void addWaitingForPlayer(Runnable r) {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            waitingForPlayer.add(r);
        } else {
            r.run();
        }
    }

    public void playerArrived() {
        Iterator<Runnable> it = waitingForPlayer.iterator();
        while (it.hasNext()) {
            it.next().run();
            it.remove();
        }
    }

    public DatabaseFassade getDatabaseFassade() {
        return dbf;
    }

    public SQLConfig getSQLConfigData() {
        return sqlConfig;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID id) {
        if (id == null) {
            throw new NullPointerException();
        }
        PlayerData pd = playerData.get(id);
        if (pd == null) {
            pd = new PlayerData(id);
            playerData.put(id, pd);
        }
        return pd;
    }

    public void unloadPlayerData(UUID id) {
        playerData.remove(id);
    }

    public boolean hasTreasureChest() {
        return Bukkit.getPluginManager().getPlugin("TreasureChest") != null;
    }

    public void addToTreasureChest(UUID playerId, Reward reward) {
        ItemStack display = new ItemStack(Material.BOOK);
        display.addEnchantment(Enchantment.LUCK, 0);
        ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Quest-Belohnung");
        display.setItemMeta(meta);

        TreasureChestAPI tcAPI = TreasureChest.getPlugin(TreasureChest.class);
        tcAPI.addItem(Bukkit.getOfflinePlayer(playerId), display, reward.getItems(), reward.getCubes());
    }

    public void payCubes(UUID playerId, int cubes) {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().log(Level.SEVERE, "Could not find Economy! Hence, could not pay " + cubes + " cubes to player " + playerId.toString());
            return;
        }
        EconomyResponse response = rsp.getProvider().depositPlayer(Bukkit.getOfflinePlayer(playerId), cubes);
        if (!response.transactionSuccess()) {
            getLogger().log(Level.SEVERE, "Could not pay " + cubes + " cubes to player " + playerId.toString() + " (EconomyResponse not successfull: " + response.errorMessage + ")");
        }
    }

}
