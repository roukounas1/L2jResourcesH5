/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.ai.npc.Teleports;

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * Race Track teleport AI.<br>
 * Original python script by DraX & updated by DrLecter.
 * @author Plim
 */
public class TeleportToRaceTrack extends AbstractNpcAI
{
	// NPC
	private static final int RACE_MANAGER = 30995;
	// Misc
	private static final Map<Integer, Integer> TELEPORTERS = new FastMap<>();
	// Locations
	private static final Location[] RETURN_LOCS =
	{
		new Location(-80826, 149775, -3043),
		new Location(-12672, 122776, -3116),
		new Location(15670, 142983, -2705),
		new Location(83400, 147943, -3404),
		new Location(111409, 219364, -3545),
		new Location(82956, 53162, -1495),
		new Location(146331, 25762, -2018),
		new Location(116819, 76994, -2714),
		new Location(43835, -47749, -792),
		new Location(147930, -55281, -2728),
		new Location(87386, -143246, -1293),
		new Location(12882, 181053, -3560)
	};
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return null;
		}
		
		if (TELEPORTERS.containsKey(npc.getId()))
		{
			st.getPlayer().teleToLocation(12661, 181687, -3560);
			st.setState(State.STARTED);
			st.set("id", String.valueOf(TELEPORTERS.get(npc.getId())));
		}
		else if (npc.getId() == RACE_MANAGER)
		{
			if ((st.getState() == State.STARTED) && (st.getInt("id") > 0))
			{
				int return_id = st.getInt("id") - 1;
				if (return_id < 13)
				{
					st.getPlayer().teleToLocation(RETURN_LOCS[return_id], false);
					st.unset("id");
				}
			}
			else
			{
				player.sendPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), "You've arrived here from a different way. I'll send you to Dion Castle Town which is the nearest town."));
				st.getPlayer().teleToLocation(15670, 142983, -2700);
			}
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	public TeleportToRaceTrack(String name, String descr)
	{
		super(name, descr);
		TELEPORTERS.put(30059, 3); // TRISHA
		TELEPORTERS.put(30080, 4); // CLARISSA
		TELEPORTERS.put(30177, 6); // VALENTIA
		TELEPORTERS.put(30233, 8); // ESMERALDA
		TELEPORTERS.put(30256, 2); // BELLA
		TELEPORTERS.put(30320, 1); // RICHLIN
		TELEPORTERS.put(30848, 7); // ELISA
		TELEPORTERS.put(30899, 5); // FLAUEN
		TELEPORTERS.put(31320, 9); // ILYANA
		TELEPORTERS.put(31275, 10); // TATIANA
		TELEPORTERS.put(31964, 11); // BILIA
		TELEPORTERS.put(31210, 12); // RACE TRACK GK
		
		for (int npcId : TELEPORTERS.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		addStartNpc(RACE_MANAGER);
		addTalkId(RACE_MANAGER);
	}
	
	public static void main(String[] args)
	{
		new TeleportToRaceTrack(TeleportToRaceTrack.class.getSimpleName(), "ai/npc/Teleports");
	}
}
