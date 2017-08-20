package de.iani.cubequest.quests;

import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;

public class CommandQuest extends Quest {

    private String regex;
    private boolean caseSensitive;
    private Pattern pattern;

    /**
     * Erzeugt eine CommandQuest, bei der der Spieler einen bestimmten Befehl eingeben muss.
     * Der Befehl kann durch genaue Argumente spezifiziert werden.
     * @param name Name der Quest
     * @param giveMessage Nachricht, die der Spieler beim Start der Quest erhählt.
     * @param successMessage Nachricht, die der Spieler bei Abschluss der Quest erhählt.
     * @param successReward Belohnung, die der Spieler bei Abschluss der Quest erhählt.
     * @param commands Collection der Befehle, die der Spieler eingeben kann, um die Quest zu erfüllen.
     * @param args Collection von Argumenten, die der Spieler eingeben kann, um die Quest zu erfüllen. Null-Argumente sind immer erfüllt.
     * @param caseSensitive ob die Argumente case-senstitive sind (commands sind nie case-sensitive).
     */
    public CommandQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            String regex, boolean caseSensitive) {
        super(id, name, giveMessage, successMessage, successReward);

        this.regex = regex;
        this.caseSensitive = caseSensitive;
        this.pattern = caseSensitive? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public CommandQuest(int id) {
        this(id, null, null, null, null, null, false);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        regex = yc.getString("regex");
        caseSensitive = yc.getBoolean("caseSensitive");

        pattern = caseSensitive? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("regex", regex);
        yc.set("caseSensitive", caseSensitive);

        return super.serialize(yc);
    }

    @Override
    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (!CubeQuest.getInstance().getPlayerData(event.getPlayer()).isGivenTo(this.getId())) {
            return false;
        }

        String msg = event.getMessage().substring(1);
        if (pattern.matcher(msg).matches()) {
            onSuccess(event.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public boolean isLegal() {
        return pattern != null;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String val) {
        pattern = val == null? null : caseSensitive? Pattern.compile(val) : Pattern.compile(val, Pattern.CASE_INSENSITIVE);
        this.regex = val;
    }

    public void setLiteralMatch(String val) {
        setRegex(Pattern.quote(val));
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean val) {
        pattern = regex == null? null : val? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.caseSensitive = val;
    }

}
