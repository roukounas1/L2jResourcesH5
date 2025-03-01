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
package l2r.gameserver.scripts.handlers.targethandlers;

import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.network.SystemMessageId;

/**
 * @author UnAfraid
 */
public class PartyMember implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}
		if (!target.isDead())
		{
			if ((target == activeChar) || (activeChar.isInParty() && target.isInParty() && (activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId())) || (activeChar.isPlayer() && target.isSummon() && (activeChar.getSummon() == target)) || (activeChar.isSummon() && target.isPlayer() && (activeChar == target.getSummon())))
			{
				return new L2Character[]
				{
					target
				};
			}
		}
		return _emptyTargetList;
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.PARTY_MEMBER;
	}
}
