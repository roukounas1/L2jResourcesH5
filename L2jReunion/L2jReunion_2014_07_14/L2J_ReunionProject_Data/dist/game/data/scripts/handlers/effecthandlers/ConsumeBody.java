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
package handlers.effecthandlers;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Consume Body effect.
 * @author Zoey76
 */
public class ConsumeBody extends L2Effect
{
	public ConsumeBody(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffected().isNpc() || !getEffected().isDead())
		{
			return false;
		}
		((L2Npc) getEffected()).endDecayTask();
		return true;
	}
}
