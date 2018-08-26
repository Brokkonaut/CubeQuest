package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetFollowupRequiredForSuccessCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "setFollowupRequiredForSuccess";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                        parsed -> (!(parsed[1] instanceof ComplexQuest)
                                ? "Nur ComplexQuests haben diese Eigenschaft!"
                                : null)),
                new ParameterDefiner(ParameterType.BOOLEAN, "FollowupRequiredForSuccess",
                        parsed -> null)};
        
        propertySetter = parsed -> {
            ((ComplexQuest) parsed[1]).setFollowupRequiredForSuccess((Boolean) parsed[2]);
            return null;
        };
        
        successMessageProvider = parsed -> "FollowupRequiredForSuccess für Quest "
                + ((ComplexQuest) parsed[1]).getId() + " auf " + parsed[2] + " gesetzt.";
    }
    
    public SetFollowupRequiredForSuccessCommand() {
        super("quest setFollowupRequiredForSuccess", ACCEPTING_SENDER_CONSTRAINT, parameterDefiners,
                propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (String s : AssistedSubCommand.TRUE_STRINGS) {
            result.add(s);
        }
        for (String s : AssistedSubCommand.FALSE_STRINGS) {
            result.add(s);
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<true | false>";
    }
    
}
