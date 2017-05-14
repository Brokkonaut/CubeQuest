package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public abstract class NPCQuest extends ServerDependendQuest {

    private Integer npcId;

    public NPCQuest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId,
            NPC npc) {
        super(name, giveMessage, successMessage, failMessage, successReward, failReward, serverId);

        npcId = (npc == null)? null : npc.getId();
    }

    public NPCQuest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            NPC npc) {
        super(name, giveMessage, successMessage, failMessage, successReward, failReward);

        npcId = (npc == null)? null : npc.getId();
    }

    public NPCQuest(String name, String giveMessage, String successMessage, Reward successReward, int serverId,
            NPC npc) {
        this(name, giveMessage, successMessage, null, successReward, null, serverId, npc);
    }

    public NPCQuest(String name, String giveMessage, String successMessage, Reward successReward,
            NPC npc) {
        this(name, giveMessage, successMessage, null, successReward, null, npc);
    }

    @Override
    public boolean onNPCClickEvent(NPCClickEvent event) {
        if (!isForThisServer()) {
            return false;
        }
        if (event.getNPC().getId() != npcId.intValue()) {
            return false;
        }
        if (getPlayerStatus(event.getClicker().getUniqueId()) != Status.GIVENTO) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLegal() {
        return !isForThisServer() || npcId != null;
    }

    public NPC getNPC() {
        if (npcId == null) {
            return null;
        }
        return CubeQuest.getInstance().getNPCReg().getById(npcId);
    }

    public void setNPC(NPC npc) {
        //TODO
    }

    //TODO NPCs setzen: fertigen übergeben oder Daten übergeben und dann erstellen?

}
