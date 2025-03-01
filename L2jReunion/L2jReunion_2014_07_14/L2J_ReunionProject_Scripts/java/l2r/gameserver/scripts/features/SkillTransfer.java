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
package l2r.gameserver.scripts.features;

import l2r.Config;
import l2r.gameserver.datatables.xml.ClassListData;
import l2r.gameserver.datatables.xml.SkillTreesData;
import l2r.gameserver.enums.IllegalActionPunishmentType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.model.L2SkillLearn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.scripting.scriptengine.events.ProfessionChangeEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2Script;
import l2r.gameserver.util.Util;

/**
 * Skill Transfer feature.
 * @author Zoey76
 */
public final class SkillTransfer extends L2Script
{
	private static final String HOLY_POMANDER = "HOLY_POMANDER_";
	private static final ItemHolder[] PORMANDERS =
	{
		// Cardinal (97)
		new ItemHolder(15307, 1),
		// Eva's Saint (105)
		new ItemHolder(15308, 1),
		// Shillen Saint (112)
		new ItemHolder(15309, 4)
	};
	
	private SkillTransfer()
	{
		super(-1, SkillTransfer.class.getSimpleName(), "features");
		// addProfessionChangeNotify(null);
		setOnEnterWorld(Config.SKILL_CHECK_ENABLE);
	}
	
	@Override
	public void onProfessionChange(ProfessionChangeEvent event)
	{
		final L2PcInstance player = event.getPlayer();
		final int index = getTransferClassIndex(player);
		if (index < 0)
		{
			return;
		}
		
		final String name = HOLY_POMANDER + player.getClassId().getId();
		if (!player.getVariables().getBool(name, false))
		{
			player.getVariables().set(name, true);
			giveItems(player, PORMANDERS[index]);
		}
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		addProfessionChangeNotify(player);
		final int index = getTransferClassIndex(player);
		if (index < 0)
		{
			return super.onEnterWorld(player);
		}
		
		final String name = HOLY_POMANDER + player.getClassId().getId();
		if (!player.getVariables().getBool(name, false))
		{
			player.getVariables().set(name, true);
			giveItems(player, PORMANDERS[index]);
		}
		
		if (!player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM)
		{
			long count = PORMANDERS[index].getCount() - player.getInventory().getInventoryItemCount(PORMANDERS[index].getId(), -1, false);
			for (L2Skill sk : player.getAllSkills())
			{
				for (L2SkillLearn s : SkillTreesData.getInstance().getTransferSkillTree(player.getClassId()).values())
				{
					if (s.getSkillId() == sk.getId())
					{
						// Holy Weapon allowed for Shilien Saint/Inquisitor stance
						if ((sk.getId() == 1043) && (index == 2) && player.isInStance())
						{
							continue;
						}
						
						count--;
						if (count < 0)
						{
							final String className = ClassListData.getInstance().getClass(player.getClassId()).getClassName();
							Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has too many transfered skills or items, skill:" + s.getName() + " (" + sk.getId() + "/" + sk.getLevel() + "), class:" + className, IllegalActionPunishmentType.BROADCAST);
							if (Config.SKILL_CHECK_REMOVE)
							{
								player.removeSkill(sk);
							}
						}
					}
				}
			}
		}
		return super.onEnterWorld(player);
	}
	
	private static int getTransferClassIndex(L2PcInstance player)
	{
		switch (player.getClassId())
		{
			case cardinal:
			{
				return 0;
			}
			case evaSaint:
			{
				return 1;
			}
			case shillienSaint:
			{
				return 2;
			}
			default:
			{
				return -1;
			}
		}
	}
	
	public static void main(String[] args)
	{
		new SkillTransfer();
	}
}
