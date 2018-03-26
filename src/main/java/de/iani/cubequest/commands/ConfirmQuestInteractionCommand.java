package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.entity.Player;

public class ConfirmQuestInteractionCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] argumentDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        argumentDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.UUID, "Quest-Schlüssel", parsed -> null)};
        
        propertySetter = parsed -> {
            CubeQuest.getInstance().getInteractionConfirmationHandler()
                    .interactionConfirmedCommand((Player) parsed[0], (UUID) parsed[1]);
            return null;
        };
        
        successMessageProvider = parsed -> null;
    }
    
    public ConfirmQuestInteractionCommand() {
        super("quest confirmQuestInteraction", ACCEPTING_SENDER_CONSTRAINT, argumentDefiners,
                propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
}