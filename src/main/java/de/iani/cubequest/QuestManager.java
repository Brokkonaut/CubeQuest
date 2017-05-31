package de.iani.cubequest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;

public class QuestManager {

    private static QuestManager instance;

    private HashMap<String, HashSet<Quest>> questsByNames;
    private HashMap<Integer, Quest> questsByIds;
    private HashMap<Integer, HashSet<ComplexQuest>> waitingForQuest;

    public static QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
         return instance;
    }

    private QuestManager() {
        questsByNames = new HashMap<String, HashSet<Quest>>();
        questsByIds = new HashMap<Integer, Quest>();
        waitingForQuest = new HashMap<Integer, HashSet<ComplexQuest>>();
    }

    public void addQuest(Quest quest) {
        questsByIds.put(quest.getId(), quest);
        addByName(quest);
        HashSet<ComplexQuest> waiting = waitingForQuest.get(quest.getId());
        if (waiting != null) {
            for (ComplexQuest cq: waiting.toArray(new ComplexQuest[0])) {
                cq.informQuestNowThere(quest);
                waiting.remove(cq);
                if (waiting.isEmpty()) {
                    waitingForQuest.remove(quest.getId());
                }
            }
        }
    }

    public void removeQuest(int id) {
        Quest quest = questsByIds.get(id);
        if (quest == null) {
            return;
        }
        questsByIds.remove(id);
        removeByName(quest);
    }

    public void removeQuest(Quest quest) {
        removeQuest(quest.getId());
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

    public void registerWaitingForQuest(ComplexQuest waiting, int waitingForId) {
        HashSet<ComplexQuest> hs = waitingForQuest.get(waitingForId);
        if (hs == null) {
            hs = new HashSet<ComplexQuest>();
            waitingForQuest.put(waitingForId, hs);
        }
        hs.add(waiting);
    }

}