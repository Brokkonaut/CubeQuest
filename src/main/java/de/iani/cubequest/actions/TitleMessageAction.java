package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;


public class TitleMessageAction extends QuestAction {
    
    private String title;
    private String subtitle;
    
    private int fadeIn;
    private int stay;
    private int fadeOut;
    
    public TitleMessageAction(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = Objects.requireNonNull(title);
        this.subtitle = Objects.requireNonNull(subtitle);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }
    
    public TitleMessageAction(Map<String, Object> serialized) {
        this.title = (String) serialized.get("title");
        this.subtitle = (String) serialized.get("subtitle");
        this.fadeIn = (Integer) serialized.get("fadeIn");
        this.stay = (Integer) serialized.get("stay");
        this.fadeOut = (Integer) serialized.get("fadeOut");
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        String individualTitle = MessageAction.PLAYER_NAME_PATTERN.matcher(this.title).replaceAll(player.getName());
        String individualSubtitle =
                MessageAction.PLAYER_NAME_PATTERN.matcher(this.subtitle).replaceAll(player.getName());
        
        player.sendTitle(individualTitle, individualSubtitle, this.fadeIn, this.stay, this.fadeOut);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        TextComponent tagComp =
                new TextComponent("Titel (" + this.fadeIn + " in, " + this.stay + " stay, " + this.fadeOut + "out): ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        TextComponent titleComp = new TextComponent(TextComponent.fromLegacyText(this.title));
        resultMsg[0].addExtra(titleComp);
        
        TextComponent betweenComp = new TextComponent(" | ");
        resultMsg[0].addExtra(betweenComp);
        
        TextComponent subtitleComp = new TextComponent(TextComponent.fromLegacyText(this.subtitle));
        resultMsg[0].addExtra(subtitleComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", this.title);
        result.put("subtitle", this.subtitle);
        result.put("fadeIn", this.fadeIn);
        result.put("stay", this.stay);
        result.put("fadeOut", this.fadeOut);
        return result;
    }
    
}
