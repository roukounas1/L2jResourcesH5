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
package l2r.gameserver.scripts.instances;

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Pailaka (Devil's Isle) instance zone.
 */
public class PailakaDevilsLegacy extends Quest
{
	private static final int MIN_LEVEL = 61;
	private static final int MAX_LEVEL = 67;
	private static final int EXIT_TIME = 5;
	private static final int TEMPLATE_ID = 44;
	protected static final int[] TELEPORT =
	{
		76428,
		-219038,
		-3752
	};
	private static final int ZONE = 20109;
	
	private static final int SURVIVOR = 32498;
	private static final int SUPPORTER = 32501;
	private static final int ADVENTURER1 = 32508;
	private static final int ADVENTURER2 = 32511;
	
	private static final int KAMS = 18629;
	private static final int HIKORO = 18630;
	private static final int ALKASO = 18631;
	private static final int GERBERA = 18632;
	private static final int LEMATAN = 18633;
	private static final int FOLLOWERS = 18634;
	private static final int TREASURE_BOX = 32495;
	private static final int POWDER_KEG = 18622;
	private static final int[] MONSTERS =
	{
		KAMS,
		HIKORO,
		ALKASO,
		GERBERA,
		LEMATAN,
		FOLLOWERS,
		TREASURE_BOX,
		POWDER_KEG,
		18623,
		18624,
		18625,
		18626,
		18627
	};
	
	private static final int SWORD = 13042;
	private static final int ENH_SWORD1 = 13043;
	private static final int ENH_SWORD2 = 13044;
	private static final int SCROLL_1 = 13046;
	private static final int SCROLL_2 = 13047;
	private static final int HEALING_POTION = 13033;
	private static final int ANTIDOTE_POTION = 13048;
	private static final int DIVINE_POTION = 13049;
	private static final int DEFENCE_POTION = 13059;
	private static final int PAILAKA_KEY = 13150;
	
	private static boolean _isTeleportScheduled = false;
	private static boolean _isOnShip = false;
	private static L2Npc _lematanNpc = null;
	private List<L2Npc> _followerslist;
	
	private static final int[] ITEMS =
	{
		SWORD,
		ENH_SWORD1,
		ENH_SWORD2,
		SCROLL_1,
		SCROLL_2,
		HEALING_POTION,
		ANTIDOTE_POTION,
		DIVINE_POTION,
		DEFENCE_POTION,
		PAILAKA_KEY
	};
	
	// @formatter:off
	private static final int[][] DROPLIST =
	{
		// must be sorted by npcId !
		// npcId, itemId, chance, max
		{ TREASURE_BOX, HEALING_POTION, 20 },
		{ TREASURE_BOX, DIVINE_POTION, 40 },
		{ TREASURE_BOX, DEFENCE_POTION, 60 },
		{ TREASURE_BOX, PAILAKA_KEY, 80 },
		{ TREASURE_BOX, ANTIDOTE_POTION, 100 }
	};
	
	private static final int[][] HP_HERBS_DROPLIST = 
	{
		// itemId, count, chance
		{ 8602, 1, 10 },
		{ 8601, 1, 40 },
		{ 8600, 1, 70 }
	};
	
	private static final int[][] MP_HERBS_DROPLIST =
	{
		// itemId, count, chance
		{ 8605, 1, 10 },
		{ 8604, 1, 40 },
		{ 8603, 1, 70 }
	};
	
	private static final int[] REWARDS =
	{
		13295,
		13129
	};
	
	private static final int[][] FOLLOWERS_SPAWNS =
	{
		{ 85067, -208943, -3336, 20106, 60 },
		{ 84904, -208944, -3336, 10904, 60 },
		{ 85062, -208538, -3336, 44884, 60 },
		{ 84897, -208542, -3336, 52973, 60 },
		{ 84808, -208633, -3339, 65039, 60 },
		{ 84808, -208856, -3339,     0, 60 },
		{ 85144, -208855, -3341, 33380, 60 },
		{ 85139, -208630, -3339, 31777, 60 }
	};
	// @formatter:on
	
	private static L2Skill boom_skill = SkillData.getInstance().getInfo(5714, 1);
	private static L2Skill energy_skill = SkillData.getInstance().getInfo(5712, 1);
	
	private void attackPlayer(L2Attackable npc, L2Playable attacker)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(attacker, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
	}
	
	private static final void dropHerb(L2Npc mob, L2PcInstance player, int[][] drop)
	{
		final int chance = getRandom(100);
		for (int[] element : drop)
		{
			if (chance < element[2])
			{
				mob.dropItem(player, element[0], element[1]);
				return;
			}
		}
	}
	
	private static final void dropItem(L2Npc mob, L2PcInstance player)
	{
		final int npcId = mob.getId();
		final int chance = getRandom(100);
		for (int[] drop : DROPLIST)
		{
			if (npcId == drop[0])
			{
				if (chance < drop[2])
				{
					mob.dropItem(player, drop[1], getRandom(1, 6));
					return;
				}
			}
			if (npcId < drop[0])
			{
				return; // not found
			}
		}
	}
	
	protected static final void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}
	
	private final synchronized void enterInstance(L2PcInstance player)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.getTemplateId() == TEMPLATE_ID)
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				teleportPlayer(player, TELEPORT, world.getInstanceId());
			}
			return;
		}
		// New instance
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("PailakaDevilsLegacy.xml");
		world = new InstanceWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(TEMPLATE_ID);
		InstanceManager.getInstance().addWorld(world);
		
		world.addAllowed(player.getObjectId());
		teleportPlayer(player, TELEPORT, instanceId);
		
		_lematanNpc = addSpawn(LEMATAN, 88108, -209252, -3744, 64255, false, 0, false, instanceId);
		
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		// Must be here, because of player == null
		if ((npc.getId() == FOLLOWERS) && event.equals("follower_cast"))
		{
			if (!npc.isCastingNow() && !npc.isDead() && !_lematanNpc.isDead())
			{
				npc.setTarget(_lematanNpc);
				npc.doCast(energy_skill);
			}
			startQuestTimer("follower_cast", 2000 + getRandom(100, 1000), npc, null);
			return null;
		}
		else if ((npc.getId() == POWDER_KEG) && event.equalsIgnoreCase("keg_trigger"))
		{
			onAttack(npc, player, 600, false);
		}
		
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player);
			if (st.isCond(1))
			{
				st.setCond(2, true);
				return "32498-07.htm";
			}
			return "32498-09.htm";
		}
		else if (event.equalsIgnoreCase("32498-05.htm"))
		{
			if (st.isCond(0))
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("32501-03.htm"))
		{
			if (st.isCond(2))
			{
				giveItems(player, SWORD, 1);
				st.setCond(3, true);
			}
		}
		else if (event.equalsIgnoreCase("32510-02.htm"))
		{
			st.exitQuest(false, true);
			
			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			inst.setDuration(EXIT_TIME * 60000);
			inst.setEmptyDestroyTime(0);
			
			if (inst.containsPlayer(player.getObjectId()))
			{
				player.setVitalityPoints(20000, true);
				st.addExpAndSp(810000, 50000);
				for (int id : REWARDS)
				{
					giveItems(player, id, 1);
				}
			}
		}
		else if (event.equalsIgnoreCase("lematan_teleport"))
		{
			if ((npc.getId() == LEMATAN) && !npc.isMovementDisabled() && !_isOnShip)
			{
				// Reduce Hate
				((L2Attackable) npc).reduceHate(player, 9999);
				((L2Attackable) npc).abortAttack();
				((L2Attackable) npc).abortCast();
				// Broadcast Escape
				npc.broadcastPacket(new MagicSkillUse(npc, 2100, 1, 1000, 0));
				// Schedule telport - when Lematan Finish casting
				startQuestTimer("lematan_finish_teleport", 1500, npc, player);
			}
			else
			{
				_isTeleportScheduled = false;
			}
			return null;
		}
		else if ((npc.getId() == LEMATAN) && event.equalsIgnoreCase("lematan_finish_teleport") && !_isOnShip)
		{
			// Teleport Lematan
			npc.teleToLocation(84973, -208721, -3340);
			// Set onShip
			_isOnShip = true;
			// Set Spawn loc to ship. If he loose aggro he should stay on board ;)
			npc.getSpawn().setX(84973);
			npc.getSpawn().setY(-208721);
			npc.getSpawn().setZ(-3340);
			// To be sure, reduce again
			((L2Attackable) npc).reduceHate(player, 9999);
			// Spawn followers
			_followerslist = new FastList<>();
			for (int[] SPAWN : FOLLOWERS_SPAWNS)
			{
				L2Npc _follower = addSpawn(FOLLOWERS, SPAWN[0], SPAWN[1], SPAWN[2], SPAWN[3], false, 0, true, player.getInstanceId());
				if (_follower != null)
				{
					_followerslist.add(_follower);
				}
			}
			return null;
		}
		return event;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if ((npc.getId() != ADVENTURER2) || (st == null) || !st.isCompleted())
		{
			return npc.getId() + ".htm";
		}
		return "32511-03.htm";
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (npc.getId())
		{
			case SURVIVOR:
				switch (st.getState())
				{
					case State.CREATED:
						if (player.getLevel() < MIN_LEVEL)
						{
							return "32498-11.htm";
						}
						if (player.getLevel() > MAX_LEVEL)
						{
							return "32498-12.htm";
						}
						return "32498-01.htm";
					case State.STARTED:
						if (st.getCond() > 1)
						{
							return "32498-08.htm";
						}
						return "32498-06.htm";
					case State.COMPLETED:
						return "32498-10.htm";
					default:
						return "32498-01.htm";
				}
			case SUPPORTER:
				if (st.getCond() > 2)
				{
					return "32501-04.htm";
				}
				return "32501-01.htm";
			case ADVENTURER1:
				if (!player.hasSummon())
				{
					if (hasQuestItems(player, SWORD))
					{
						if (hasQuestItems(player, SCROLL_1))
						{
							takeItems(player, SWORD, -1);
							takeItems(player, SCROLL_1, -1);
							giveItems(player, ENH_SWORD1, 1);
							return "32508-03.htm";
						}
						return "32508-02.htm";
					}
					
					if (hasQuestItems(player, ENH_SWORD1))
					{
						if (hasQuestItems(player, SCROLL_2))
						{
							takeItems(player, ENH_SWORD1, -1);
							takeItems(player, SCROLL_2, -1);
							giveItems(player, ENH_SWORD2, 1);
							return "32508-05.htm";
						}
						return "32508-04.htm";
					}
					
					if (hasQuestItems(player, ENH_SWORD2))
					{
						return "32508-06.htm";
					}
					return "32508-00.htm";
				}
				return "32508-07.htm";
			case ADVENTURER2:
				if (!player.hasSummon())
				{
					st.exitQuest(false, true);
					
					Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
					inst.setDuration(EXIT_TIME * 60000);
					inst.setEmptyDestroyTime(0);
					
					if (inst.containsPlayer(player.getObjectId()))
					{
						player.setVitalityPoints(20000, true);
						addExpAndSp(player, 10800000, 950000);
						for (int id : REWARDS)
						{
							giveItems(player, id, 1);
						}
					}
					return "32511-01.htm";
				}
				return "32511-02.htm";
		}
		return getNoQuestMsg(player);
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() == POWDER_KEG) && !npc.isDead())
		{
			npc.doCast(boom_skill);
			
			if (npc.getKnownList() != null)
			{
				for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(900))
				{
					target.reduceCurrentHp(500 + getRandom(0, 200), npc, boom_skill);
					
					if (target instanceof L2MonsterInstance)
					{
						if (((L2MonsterInstance) target).getId() == POWDER_KEG)
						{
							startQuestTimer("keg_trigger", 500, (L2Npc) target, attacker);
						}
						else
						{
							if (isSummon)
							{
								attackPlayer((L2Attackable) npc, attacker.getSummon());
							}
							else
							{
								attackPlayer((L2Attackable) npc, attacker);
							}
						}
					}
				}
			}
			if (!npc.isDead())
			{
				npc.doDie(attacker);
			}
		}
		else if ((npc.getId() == LEMATAN) && (npc.getCurrentHp() < (npc.getMaxHp() / 2)) && !_isTeleportScheduled)
		{
			startQuestTimer("lematan_teleport", 1000, npc, attacker);
		}
		else if (npc.getId() == TREASURE_BOX)
		{
			dropItem(npc, attacker);
			npc.doDie(attacker);
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(getName());
		if ((st != null) && st.isStarted())
		{
			switch (npc.getId())
			{
				case KAMS:
					if (hasQuestItems(player, SWORD))
					{
						giveItems(player, SCROLL_1, 1);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				case ALKASO:
					if (hasQuestItems(player, ENH_SWORD1))
					{
						giveItems(player, SCROLL_2, 1);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				case LEMATAN:
					if ((_followerslist != null) && !_followerslist.isEmpty())
					{
						for (L2Npc _follower : _followerslist)
						{
							_follower.deleteMe();
						}
						_followerslist.clear();
					}
					st.setCond(4, true);
					addSpawn(ADVENTURER2, 84983, -208736, -3336, 49915, false, 0, false, npc.getInstanceId());
					break;
				case POWDER_KEG:
				case TREASURE_BOX:
				case FOLLOWERS:
					// do nothing
					break;
				default:
					// hardcoded herb drops
					dropHerb(npc, player, HP_HERBS_DROPLIST);
					dropHerb(npc, player, MP_HERBS_DROPLIST);
					break;
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		startQuestTimer("follower_cast", 1000 + getRandom(100, 1000), npc, null);
		npc.disableCoreAI(true);
		return null;
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if ((character instanceof L2PcInstance) && !character.isDead() && !character.isTeleporting() && ((L2PcInstance) character).isOnline())
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character, world.getInstanceId()), 1000);
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	static final class Teleport implements Runnable
	{
		private final L2Character _char;
		private final int _instanceId;
		
		public Teleport(L2Character c, int id)
		{
			_char = c;
			_instanceId = id;
		}
		
		@Override
		public void run()
		{
			try
			{
				teleportPlayer((L2PcInstance) _char, TELEPORT, _instanceId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private PailakaDevilsLegacy()
	{
		super(129, "129_PailakaDevilsLegacy", "Pailaka - Devil's Legacy");
		addStartNpc(SURVIVOR);
		addFirstTalkId(SURVIVOR, SUPPORTER, ADVENTURER1, ADVENTURER2);
		addTalkId(SURVIVOR, SUPPORTER, ADVENTURER1, ADVENTURER2);
		addAttackId(TREASURE_BOX, POWDER_KEG, LEMATAN);
		addKillId(MONSTERS);
		addEnterZoneId(ZONE);
		addSpawnId(FOLLOWERS);
		registerQuestItems(ITEMS);
	}
	
	public static void main(String[] args)
	{
		new PailakaDevilsLegacy();
	}
}