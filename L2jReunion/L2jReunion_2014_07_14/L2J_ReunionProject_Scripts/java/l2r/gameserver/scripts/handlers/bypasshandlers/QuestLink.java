/*
 * Copyright (C) 2004-2014 L2J DataPack
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
package l2r.gameserver.scripts.handlers.bypasshandlers;

import java.util.List;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.handler.IBypassHandler;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.util.StringUtil;

public class QuestLink implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Quest"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		String quest = "";
		try
		{
			quest = command.substring(5).trim();
		}
		catch (IndexOutOfBoundsException ioobe)
		{
		}
		if (quest.length() == 0)
		{
			showQuestWindow(activeChar, (L2Npc) target);
		}
		else
		{
			int questNameEnd = quest.indexOf(" ");
			if (questNameEnd == -1)
			{
				showQuestWindow(activeChar, (L2Npc) target, quest);
			}
			else
			{
				activeChar.processQuestEvent(quest.substring(0, questNameEnd), quest.substring(questNameEnd).trim());
			}
		}
		return true;
	}
	
	/**
	 * Open a choose quest window on client with all quests available of the L2NpcInstance.<br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param npc The table containing quests of the L2NpcInstance
	 * @param quests
	 */
	public static void showQuestChooseWindow(L2PcInstance player, L2Npc npc, Quest[] quests)
	{
		final StringBuilder sb = StringUtil.startAppend(150, "<html><body>");
		String state = "";
		int questId = -1;
		for (Quest q : quests)
		{
			if (q == null)
			{
				continue;
			}
			
			StringUtil.append(sb, "<a action=\"bypass -h npc_", String.valueOf(npc.getObjectId()), "_Quest ", q.getName(), "\">[");
			final QuestState qs = player.getQuestState(q.getScriptName());
			if ((qs == null) || qs.isCreated())
			{
				state = q.isCustomQuest() ? "" : "01";
			}
			else if (qs.isStarted())
			{
				state = q.isCustomQuest() ? " (In Progress)" : "02";
			}
			else if (qs.isCompleted())
			{
				state = q.isCustomQuest() ? " (Done)" : "03";
			}
			
			if (q.isCustomQuest())
			{
				StringUtil.append(sb, q.getDescr(), state);
			}
			else
			{
				questId = q.getId();
				if (q.getId() > 10000)
				{
					questId -= 5000;
				}
				else if (questId == 146)
				{
					questId = 640;
				}
				StringUtil.append(sb, "<fstring>", String.valueOf(questId), state, "</fstring>");
			}
			sb.append("]</a><br>");
		}
		sb.append("</body></html>");
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		npc.insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	/**
	 * Open a quest window on client with the text of the L2NpcInstance.<br>
	 * <b><u>Actions</u>:</b><br>
	 * <ul>
	 * <li>Get the text of the quest state in the folder data/scripts/quests/questId/stateId.htm</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li>
	 * </ul>
	 * @param player the L2PcInstance that talk with the {@code npc}
	 * @param npc the L2NpcInstance that chats with the {@code player}
	 * @param questId the Id of the quest to display the message
	 */
	@SuppressWarnings("null")
	public static void showQuestWindow(L2PcInstance player, L2Npc npc, String questId)
	{
		String content = null;
		
		Quest q = QuestManager.getInstance().getQuest(questId);
		
		// Get the state of the selected quest
		QuestState qs = player.getQuestState(questId);
		
		if (q != null)
		{
			if (((q.getId() >= 1) && (q.getId() < 20000)) && ((player.getWeightPenalty() >= 3) || !player.isInventoryUnder90(true)))
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}
			
			if (qs == null)
			{
				if ((q.getId() >= 1) && (q.getId() < 20000))
				{
					// Too many ongoing quests.
					if (player.getAllActiveQuests().length > 40)
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
						html.setFile(player.getHtmlPrefix(), "data/html/fullquest.html");
						player.sendPacket(html);
						return;
					}
				}
				// check for start point
				List<Quest> qlst = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
				
				if ((qlst != null) && !qlst.isEmpty())
				{
					for (Quest temp : qlst)
					{
						if (temp == q)
						{
							qs = q.newQuestState(player);
							break;
						}
					}
				}
			}
		}
		else
		{
			content = Quest.getNoQuestMsg(player); // no quests found
		}
		
		if (qs != null)
		{
			try
			{
				// If the quest is already started, no need to show a window
				if (!qs.getQuest().notifyTalk(npc, qs))
				{
					return;
				}
			}
			catch (Exception e)
			{
				_log.error("QuestLink[notifyTalk] quest name is: " + qs.getQuest().getName());
				_log.error("QuestLink[notifyTalk] player is: " + player.getName());
				
				if (npc == null)
				{
					_log.error("QuestLink[notifyTalk] NPC is NULL");
				}
				else
				{
					_log.error("QuestLink[notifyTalk] NPC is: " + npc.getId());
				}
			}
			
			questId = qs.getQuest().getName();
			String stateId = State.getStateName(qs.getState());
			String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), path); // TODO path for quests html
			
			if (Config.DEBUG)
			{
				if (content != null)
				{
					_log.info("Showing quest window for quest " + questId + " html path: " + path);
				}
				else
				{
					_log.info("File not exists for quest " + questId + " html path: " + path);
				}
			}
		}
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		if (content != null)
		{
			npc.insertObjectIdAndShowChatWindow(player, content);
		}
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.
	 * @param player the L2PcInstance that talk with the {@code npc}.
	 * @param npc the L2NpcInstance that chats with the {@code player}.
	 */
	public static void showQuestWindow(L2PcInstance player, L2Npc npc)
	{
		// collect awaiting quests and start points
		List<Quest> options = new FastList<>();
		
		QuestState[] awaits = player.getQuestsForTalk(npc.getTemplate().getId());
		List<Quest> starts = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
		
		// Quests are limited between 1 and 999 because those are the quests that are supported by the client.
		// By limiting them there, we are allowed to create custom quests at higher IDs without interfering
		if (awaits != null)
		{
			for (QuestState x : awaits)
			{
				if (!options.contains(x.getQuest()))
				{
					if ((x.getQuest().getId() > 0) && (x.getQuest().getId() < 20000))
					{
						options.add(x.getQuest());
					}
				}
			}
		}
		
		if (starts != null)
		{
			for (Quest x : starts)
			{
				if (!options.contains(x))
				{
					if ((x.getId() > 0) && (x.getId() < 20000))
					{
						options.add(x);
					}
				}
			}
		}
		
		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, npc, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, npc, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, npc, "");
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
