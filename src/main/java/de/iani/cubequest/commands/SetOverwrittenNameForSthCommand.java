package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.CommandQuest;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.quests.Quest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class SetOverwrittenNameForSthCommand extends AssistedSubCommand {
    
    public enum SpecificSth {
        INTERACTOR("InteractorName", InteractorQuest.class, "setInteractorName"),
        LOCATION("LocationName", GotoQuest.class, "setLocationName"),
        COMMAND("CommandName", CommandQuest.class, "setCommandName");
        
        public final String propertyName;
        public final String setCommand;
        public final String resetCommand;
        public final Class<? extends Quest> questClass;
        public final Method setterMethod;
        
        private SpecificSth(String propertyName, Class<? extends Quest> questClass,
                String setterMethodName) {
            this.propertyName = propertyName;
            this.setCommand = "setQuest" + propertyName;
            this.resetCommand = "resetQuest" + propertyName;
            this.questClass = questClass;
            try {
                this.setterMethod = questClass.getMethod(setterMethodName, String.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new AssertionError(e);
            }
        }
    }
    
    private static ParameterDefiner[] getParameterDefiners(SpecificSth sth, boolean set) {
        ParameterDefiner[] result = new ParameterDefiner[set ? 2 : 1];
        result[0] = new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                parsed -> (!sth.questClass.isInstance(parsed[1])
                        ? "Nur " + sth.questClass.getSimpleName() + "s haben diese Eigenschaft!"
                        : null));
        if (set) {
            result[1] = new ParameterDefiner(ParameterType.STRING, "Name", parsed -> null);
        }
        
        return result;
    }
    
    private static Function<Object[], String> getPropertySetter(SpecificSth sth, boolean set) {
        return parsed -> {
            try {
                sth.setterMethod.invoke(parsed[1], set ? parsed[2] : null);
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }
    
    private static Function<Object[], String> getSuccessMessageProvider(SpecificSth sth,
            boolean set) {
        return parsed -> {
            return sth.propertyName + " für Quest " + ((Quest) parsed[1]).getId()
                    + (set ? " auf " + parsed[2] + " " : " zurück") + "gesetzt.";
        };
    }
    
    public SetOverwrittenNameForSthCommand(SpecificSth sth, boolean set) {
        super("quest " + sth.setCommand, AssistedSubCommand.ACCEPTING_SENDER_CONSTRAINT,
                getParameterDefiners(sth, set), getPropertySetter(sth, set),
                getSuccessMessageProvider(sth, set));
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
