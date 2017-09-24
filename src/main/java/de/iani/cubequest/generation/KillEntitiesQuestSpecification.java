package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.quests.KillEntitiesQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;

public class KillEntitiesQuestSpecification extends QuestSpecification {

    public static class KillEntitiesQuestPossibilitiesSpecification implements ConfigurationSerializable {

        private static KillEntitiesQuestPossibilitiesSpecification instance;

        private Set<EntityTypeCombination> entityTypeCombinations;

        public static KillEntitiesQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new KillEntitiesQuestPossibilitiesSpecification();
            }
            return instance;
        }

        public static KillEntitiesQuestPossibilitiesSpecification deserialize(Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException("tried to initialize a second object of singleton");
                }
            }
            instance = new KillEntitiesQuestPossibilitiesSpecification(serialized);
            return instance;
        }

        private KillEntitiesQuestPossibilitiesSpecification() {
            Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());

            this.entityTypeCombinations = new HashSet<EntityTypeCombination>();
        }

        @SuppressWarnings("unchecked")
        private KillEntitiesQuestPossibilitiesSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
            try {
                entityTypeCombinations = new HashSet<EntityTypeCombination>((List<EntityTypeCombination>) serialized.get("entityTypeCombinations"));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }

        public Set<EntityTypeCombination> getMaterialCombinations() {
            return Collections.unmodifiableSet(entityTypeCombinations);
        }

        public boolean addMaterialCombination(EntityTypeCombination mc) {
            return entityTypeCombinations.add(mc);
        }

        public boolean removeMaterialCombination(EntityTypeCombination mc) {
            return entityTypeCombinations.remove(mc);
        }

        public void clearMaterialCombinations() {
            entityTypeCombinations.clear();
        }

        public int getWeighting() {
            return isLegal()? (int) entityTypeCombinations.stream().filter(c -> c.isLegal()).count() : 0;
        }

        public boolean isLegal() {
            return entityTypeCombinations.stream().anyMatch(c -> c.isLegal());
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("entityTypeCombinations", new ArrayList<EntityTypeCombination>(entityTypeCombinations));

            return result;
        }

    }

    private EntityTypeCombination preparedEntityTypes;
    private int preparedAmount;

    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble()*0.9);

        List<EntityTypeCombination> eCombs = new ArrayList<EntityTypeCombination>(KillEntitiesQuestPossibilitiesSpecification.getInstance().getMaterialCombinations());
        eCombs.removeIf(c -> !c.isLegal());
        eCombs.sort(EntityTypeCombination.COMPARATOR);
        Collections.shuffle(eCombs, ran);
        EntityTypeCombination entityCombination = Util.randomElement(eCombs, ran);

        preparedAmount = (int) Math.ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(
                entityCombination.getContent().stream().min((m1, m2) -> {
                    return Double.compare(QuestGenerator.getInstance().getValue(m1), QuestGenerator.getInstance().getValue(m2));
                }).get()));

        return gotoDifficulty;
    }

    @Override
    public void clearGeneratedQuest() {
        preparedEntityTypes = null;
        preparedAmount = 0;
    }

    @Override
    public KillEntitiesQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated BlockPlaceQuest!", e);
            return null;
        }

        String giveMessage = "Töte " + buildKillEntitiesString(preparedEntityTypes.getContent(), preparedAmount) + ".";

        KillEntitiesQuest result = new KillEntitiesQuest(questId, questName, giveMessage, null, successReward, preparedEntityTypes.getContent(), preparedAmount);
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        clearGeneratedQuest();
        return result;
    }

    public String buildKillEntitiesString(Collection<EntityType> types, int amount) {
        String result = amount + " ";

        for (EntityType type: types) {
            result += ChatAndTextUtil.capitalize(type.name(), true) + "-";
            result += ", ";
        }

        result = ChatAndTextUtil.replaceLast(result, "-", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");

        result += "mobs";

        return result;
    }

    @Override
    public int compareTo(QuestSpecification other) {
        return super.compare(other);
    }

    @Override
    public boolean isLegal() {
        return DeliveryQuestPossibilitiesSpecification.getInstance().isLegal();
    }

    /**
     * Throws UnsupportedOperationException! Not actually serializable!
     * @return nothing
     * @throws UnsupportedOperationException always
     */
    @Override
    public Map<String, Object> serialize() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
