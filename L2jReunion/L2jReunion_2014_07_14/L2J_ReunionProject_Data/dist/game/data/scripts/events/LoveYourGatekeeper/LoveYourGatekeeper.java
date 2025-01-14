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
package events.LoveYourGatekeeper;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.event.LongTimeEvent;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Love Your Gatekeeper event.
 * @author Gladicek
 */
public class LoveYourGatekeeper extends LongTimeEvent
{
	// NCP
	private static final int GATEKEEPER = 32477;
	// Item
	private static final int GATEKEEPER_TRANSFORMATION_STICK = 12814;
	// Misc
	private static final int HOURS = 24;
	private static final int PRICE = 10000;
	// Skills
	private static SkillHolder TELEPORTER_TRANSFORM = new SkillHolder(5655, 1);
	
	public LoveYourGatekeeper()
	{
		super(LoveYourGatekeeper.class.getSimpleName(), "events");
		addStartNpc(GATEKEEPER);
		addFirstTalkId(GATEKEEPER);
		addTalkId(GATEKEEPER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "transform_stick":
			{
				if (player.getAdena() >= PRICE)
				{
					long _reuse = 0;
					String _streuse = st.get("reuse");
					if (_streuse != null)
					{
						_reuse = Long.parseLong(_streuse);
					}
					if (_reuse > System.currentTimeMillis())
					{
						final long remainingTime = (_reuse - System.currentTimeMillis()) / 1000;
						final int hours = (int) (remainingTime / 3600);
						final int minutes = (int) ((remainingTime % 3600) / 60);
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
						sm.addItemName(GATEKEEPER_TRANSFORMATION_STICK);
						sm.addInt(hours);
						sm.addInt(minutes);
						player.sendPacket(sm);
					}
					else
					{
						st.takeItems(Inventory.ADENA_ID, PRICE);
						st.giveItems(GATEKEEPER_TRANSFORMATION_STICK, 1);
						st.setState(State.STARTED);
						st.set("reuse", String.valueOf(System.currentTimeMillis() + (HOURS * 3600000)));
					}
					
				}
				else
				{
					return "32477-3.htm";
				}
				return null;
			}
			case "transform":
			{
				if (player.isTransformed())
				{
					return null;
				}
				player.doCast(TELEPORTER_TRANSFORM.getSkill());
				return null;
			}
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		return "32477.htm";
	}
	
	public static void main(String[] args)
	{
		new LoveYourGatekeeper();
	}
}
