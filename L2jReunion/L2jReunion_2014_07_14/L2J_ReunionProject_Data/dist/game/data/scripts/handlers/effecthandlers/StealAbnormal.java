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

import java.util.List;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Steal Abnormal effect implementation.
 * @author Adry_85, Zoey76
 */
public class StealAbnormal extends L2Effect
{
	public StealAbnormal(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return false;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() != null) && getEffected().isPlayer() && (getEffector() != getEffected()))
		{
			final List<L2Effect> toSteal = Formulas.calcCancelStealEffects(getEffector(), getEffected(), getSkill(), getEffectPower());
			if (toSteal.isEmpty())
			{
				return false;
			}
			final Env env = new Env();
			env.setCharacter(getEffected());
			env.setTarget(getEffector());
			for (L2Effect eff : toSteal)
			{
				env.setSkill(eff.getSkill());
				final L2Effect effect = eff.getEffectTemplate().getStolenEffect(env, eff);
				if (effect != null)
				{
					effect.scheduleEffect();
					if (effect.getShowIcon() && getEffector().isPlayer())
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(effect);
						getEffector().sendPacket(sm);
					}
				}
				eff.exit();
			}
			return true;
		}
		return false;
	}
}
