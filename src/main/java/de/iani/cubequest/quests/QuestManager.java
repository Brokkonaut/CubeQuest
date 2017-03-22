package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.iani.cubequest.events.QuestRenameEvent;

public class QuestManager {

    private static QuestManager instance;

    private HashMap<String, HashSet<Quest>> questsByNames;
    private HashMap<Integer, Quest> questsByIds;
    private int nextId;

    public static QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
         return instance;
    }

    private QuestManager() {
        questsByNames = new HashMap<String, HashSet<Quest>>();
        questsByIds = new HashMap<Integer, Quest>();
        nextId = 1;
    }

    public void addQuest(Quest quest) {
        if (quest.getId() != null) {
            throw new IllegalArgumentException("Quest already managed.");
        }
        quest.setId(nextId);
        nextId ++;
        questsByIds.put(quest.getId(), quest);
        addByName(quest);
    }

    public void onQuestRenameEvent(QuestRenameEvent event) {
        removeByName(event.getQuest());
        addByName(event.getQuest(), event.getNewName());
    }

    public Quest getQuest(int id) {
        return questsByIds.get(id);
    }

    /**
     * Gibt alle Quests mit einem Namen zurück.
     * @param name Quests mit diesem Namen sollen zurückgegeben werden.
     * @return leeres HashSet wenn es keine Quests mit diesem Namen gibt, ein unmodifizierbares HashSet (live-Objekt) mit den Quests sonst.
     */
    public Set<Quest> getQuests(String name) {
        if (questsByNames.get(name) == null) {
            return new HashSet<Quest>();
        }
        return Collections.unmodifiableSet(questsByNames.get(name));
    }

    /**
     * @return alle Quests als unmodifiableCollection (live-Object der values der HashMap, keine Kopie)
     */
    public Collection<Quest> getQuests() {
        return Collections.unmodifiableCollection(questsByIds.values());
    }

    private void addByName(Quest quest) {
        addByName(quest, quest.getName());
    }

    private void addByName(Quest quest, String name) {
        HashSet<Quest> hs = questsByNames.get(quest.getName());
        if (hs == null) {
            hs = new HashSet<Quest>();
            questsByNames.put(quest.getName(), hs);
        }
        hs.add(quest);
    }

    private void removeByName(Quest quest) {
        HashSet<Quest> hs = questsByNames.get(quest.getName());
        if (hs == null) {
            return;
        }
        hs.remove(quest);
    }

}
