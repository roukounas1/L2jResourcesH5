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

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.stats.Env;

/**
 * Sweeper effect.
 * @author Zoey76
 */
public class Sweeper extends L2Effect
{
	public Sweeper(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffector().isPlayer() || !getEffected().isAttackable())
		{
			return false;
		}
		
		final L2PcInstance player = getEffector().getActingPlayer();
		final L2Attackable monster = (L2Attackable) getEffected();
		if (!monster.checkSpoilOwner(player, false))
		{
			return false;
		}
		
		if (!player.getInventory().checkInventorySlotsAndWeight(monster.getSpoilLootItems(), false, false))
		{
			return false;
		}
		
		ItemHolder[] items = monster.takeSweep();
		if ((items == null) || (items.length == 0))
		{
			return false;
		}
		
		for (ItemHolder item : items)
		{
			if (player.isInParty())
			{
				player.getParty().distributeItem(player, item, true, monster);
			}
			else
			{
				player.addItem("Sweeper", item, getEffected(), true);
			}
		}
		return true;
	}
}
