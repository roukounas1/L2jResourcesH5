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
package l2r.gameserver.scripts.ai.npc;

import l2r.gameserver.instancemanager.SoIManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;

public class EkimusMouth extends AbstractNpcAI
{
	public EkimusMouth(String name, String desc)
	{
		super(name, desc);
		
		addStartNpc(32537);
		addFirstTalkId(32537);
		addTalkId(32537);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("hos_enter"))
		{
			if (SoIManager.getCurrentStage() == 1)
			{
				htmltext = "32537-1.htm";
			}
			else if (SoIManager.getCurrentStage() == 4)
			{
				htmltext = "32537-2.htm";
			}
		}
		else if (event.equalsIgnoreCase("hoe_enter"))
		{
			if (SoIManager.getCurrentStage() == 1)
			{
				htmltext = "32537-3.htm";
			}
			else if (SoIManager.getCurrentStage() == 4)
			{
				htmltext = "32537-4.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == 32537)
		{
			return "32537.htm";
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new EkimusMouth(EkimusMouth.class.getSimpleName(), "ai/npc");
	}
}