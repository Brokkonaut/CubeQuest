package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;
import net.citizensnpcs.api.npc.NPC;

public class DeliveryQuestSpecification extends QuestSpecification {

    public static class DeliveryQuestPossibilitiesSpecification implements ConfigurationSerializable {

        private static DeliveryQuestPossibilitiesSpecification instance;

        private Set<DeliveryReceiverSpecification> targets;
        private Set<MaterialCombination> materialCombinations;

        public static DeliveryQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new DeliveryQuestPossibilitiesSpecification();
            }
            return instance;
        }

        public static DeliveryQuestPossibilitiesSpecification deserialize(Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException("tried to initialize a second object of singleton");
                }
            }
            instance = new DeliveryQuestPossibilitiesSpecification(serialized);
            return instance;
        }

        private DeliveryQuestPossibilitiesSpecification() {
            Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());

            this.targets = new HashSet<DeliveryReceiverSpecification>();
            this.materialCombinations = new HashSet<MaterialCombination>();
        }

        @SuppressWarnings("unchecked")
        private DeliveryQuestPossibilitiesSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
            try {
                targets = new HashSet<DeliveryReceiverSpecification>((List<DeliveryReceiverSpecification>) serialized.get("targets"));
                materialCombinations = new HashSet<MaterialCombination>((List<MaterialCombination>) serialized.get("materialCombinations"));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }

        public Set<DeliveryReceiverSpecification> getTargets() {
            return Collections.unmodifiableSet(targets);
        }

        public boolean addTarget(DeliveryReceiverSpecification target) {
            return targets.add(target);
        }

        public boolean removeTarget(DeliveryReceiverSpecification target) {
            return targets.remove(target);
        }

        public void clearTargets() {
            targets.clear();
        }

        public Set<MaterialCombination> getMaterialCombinations() {
            return Collections.unmodifiableSet(materialCombinations);
        }

        public boolean addMaterialCombination(MaterialCombination mc) {
            return materialCombinations.add(mc);
        }

        public boolean removeMaterialCombination(MaterialCombination mc) {
            return materialCombinations.remove(mc);
        }

        public void clearMaterialCombinations() {
            materialCombinations.clear();
        }

        public int getWeighting() {
            return isLegal()? Math.max((int) targets.stream().filter(t -> t.isLegal()).count(),
                    (int) materialCombinations.stream().filter(c -> c.isLegal()).count()) : 0;
        }

        public boolean isLegal() {
            return targets.stream().anyMatch(t -> t.isLegal()) && materialCombinations.stream().anyMatch(c -> c.isLegal());
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("targets", new ArrayList<DeliveryReceiverSpecification>(targets));
            result.put("materialCombinations", new ArrayList<MaterialCombination>(materialCombinations));

            return result;
        }

    }

    public static class DeliveryReceiverSpecification implements ConfigurationSerializable, Comparable<DeliveryReceiverSpecification> {

        public static final Comparator<DeliveryReceiverSpecification> COMPARATOR = (o1, o2) -> (o1.compareTo(o2));

        private Integer npcId;
        private String name;

        public DeliveryReceiverSpecification() {
            if (!CubeQuest.getInstance().hasCitizensPlugin()) {
                throw new IllegalStateException("This server doesn't have the CitizensPlugin!");
            }
        }

        public DeliveryReceiverSpecification(Map<String, Object> serialized) {
            this();

            npcId = (Integer) serialized.get("npcId");
            name = (String) serialized.get("name");

            if (npcId != null && getNPC() == null) {
                throw new IllegalArgumentException("NPC with id " + npcId + " does not exist.");
            }
        }

        public NPC getNPC() {
            return npcId == null? null : CubeQuest.getInstance().getNPCReg().getById(npcId);
        }

        public void setNPC(NPC npc) {
            npcId = npc == null? null : npc.getId();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isLegal() {
            return name != null && npcId != null && getNPC() != null;
        }

        @Override
        public int compareTo(DeliveryReceiverSpecification o) {
            if (npcId == null) {
                return o.npcId == null? 0 : 1;
            } else {
                return o.npcId == null? -1 : npcId - o.npcId;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DeliveryReceiverSpecification)) {
                return false;
            }
            DeliveryReceiverSpecification o = (DeliveryReceiverSpecification) other;
            return Objects.equals(o.npcId, npcId);
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("npcId", npcId);
            result.put("name", name);
            return result;
        }

    }

    private DeliveryReceiverSpecification preparedReceiver;
    private ItemStack[] preparedDelivery;

    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble()*0.9);

        List<DeliveryReceiverSpecification> rSpecs = new ArrayList<DeliveryReceiverSpecification>(DeliveryQuestPossibilitiesSpecification.instance.targets);
        rSpecs.removeIf(s -> !s.isLegal());
        rSpecs.sort(DeliveryReceiverSpecification.COMPARATOR);
        Collections.shuffle(rSpecs, ran);
        preparedReceiver = Util.randomElement(rSpecs, ran);

        List<MaterialCombination> mCombs = new ArrayList<MaterialCombination>(DeliveryQuestPossibilitiesSpecification.instance.materialCombinations);
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        Collections.shuffle(mCombs, ran);
        MaterialCombination materialCombination = Util.randomElement(mCombs, ran);
        List<Material> materials = new ArrayList<Material>(materialCombination.getContent());

        preparedDelivery = new ItemStack[0];

        double todoDifficulty = gotoDifficulty;
        while (todoDifficulty > 0) {
            Material type = Util.randomElement(materials, ran);
            double diffCost = QuestGenerator.getInstance().getValue(type);
            if (todoDifficulty >= type.getMaxStackSize()*diffCost) {
                preparedDelivery = ItemStackUtil.addItem(new ItemStack(type, type.getMaxStackSize()), preparedDelivery);
                todoDifficulty -= type.getMaxStackSize()*diffCost;
            } else {
                preparedDelivery = ItemStackUtil.addItem(new ItemStack(type, 1), preparedDelivery);
                todoDifficulty -= type.getMaxStackSize();
            }
        }

        return gotoDifficulty;
    }

    @Override
    public void clearGeneratedQuest() {
        preparedReceiver = null;
        preparedDelivery = null;
    }

    @Override
    public DeliveryQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated DeliveryQuest!", e);
            return null;
        }

        String giveMessage = "Liefere " + buildDeliveryString(preparedDelivery) + " an " + preparedReceiver.name + ".";

        DeliveryQuest result = new DeliveryQuest(questId, questName, null, giveMessage, null, successReward, preparedReceiver.npcId, preparedDelivery);
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        clearGeneratedQuest();
        return result;
    }

    public String buildDeliveryString(ItemStack[] delivery) {
        EnumMap<Material, Integer> items = new EnumMap<Material, Integer>(Material.class);
        Arrays.stream(delivery).forEach(item -> items.put(item.getType(), item.getAmount() + (items.containsKey(item.getType())? items.get(item.getType()) : 0)));

        String result = "";

        for (Material material: items.keySet()) {
            result += items.get(material).intValue() + " ";
            result += ItemStackUtil.toNiceString(material);
            result += ", ";
        }

        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und ");

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