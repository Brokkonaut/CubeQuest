package de.iani.cubequest.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class MaterialCombination implements ConfigurationSerializable, Comparable<MaterialCombination> {

    public static final Comparator<MaterialCombination> COMPARATOR = (o1, o2) -> (o1.compareTo(o2));

    private EnumSet<Material> content;

    public MaterialCombination() {
        content = EnumSet.noneOf(Material.class);
    }

    public MaterialCombination(ItemStack[] everyMaterialOccuringInThis) {
        this();
        for (ItemStack stack: everyMaterialOccuringInThis) {
            if (stack != null) {
                content.add(stack.getType());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public MaterialCombination(Map<String, Object> serialized) {
        content = EnumSet.noneOf(Material.class);
        List<String> materialNameList = (List<String>) serialized.get("content");
        materialNameList.forEach(materialName -> content.add(Material.valueOf(materialName)));
    }

    public Set<Material> getContent() {
        return Collections.unmodifiableSet(content);
    }

    public boolean addMaterial(Material type) {
        return content.add(type);
    }

    public boolean removeMaterial(Material type) {
        return content.remove(type);
    }

    public void clearMaterials() {
        content.clear();
    }

    public boolean isLegal() {
        return !content.isEmpty();
    }

    @Override
    public int compareTo(MaterialCombination o) {
        int res = 0;
        for (Material m: Material.values()) {
            if (content.contains(m)) {
                res ++;
            }
            if (o.content.contains(m)) {
                res --;
            }
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MaterialCombination)) {
            return false;
        }
        return ((MaterialCombination) other).content.equals(content);
    }

    public BaseComponent[] getSpecificationInfo() {
        return new ComponentBuilder(ChatColor.GREEN + content.toString()).create();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        List<String> materialNameList = new ArrayList<>();
        content.forEach(material -> materialNameList.add(material.name()));
        result.put("content", materialNameList);
        return result;
    }

}