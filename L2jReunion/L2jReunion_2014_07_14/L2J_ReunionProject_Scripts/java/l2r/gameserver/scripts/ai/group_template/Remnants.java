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
package l2r.gameserver.scripts.ai.group_template;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * Remnants AI.
 * @author DS
 */
public class Remnants extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		18463,
		18464,
		18465
	};
	private static final int SKILL_HOLY_WATER = 2358;
	
	// TODO: Find retail strings.
	// private static final String MSG = "The holy water affects Remnants Ghost. You have freed his soul.";
	// private static final String MSG_DEREK = "The holy water affects Derek. You have freed his soul.";
	private Remnants()
	{
		super(Remnants.class.getSimpleName(), "ai/group_template");
		addSpawnId(NPCS);
		addSkillSeeId(NPCS);
		// Do not override onKill for Derek here. Let's make global Hellbound manipulations in Engine where it is possible.
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (skill.getId() == SKILL_HOLY_WATER)
		{
			if (!npc.isDead())
			{
				if ((targets.length > 0) && (targets[0] == npc))
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02)) // Lower, than 2%
					{
						npc.doDie(caster);
						//@formatter:off
						/*if (npc.getId() == DEREK)
						{
							caster.sendMessage(MSG_DEREK);
						}
						else
						{
							caster.sendMessage(MSG);
						}*/
						//@formatter:on
					}
				}
			}
		}
		
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Remnants();
	}
}
