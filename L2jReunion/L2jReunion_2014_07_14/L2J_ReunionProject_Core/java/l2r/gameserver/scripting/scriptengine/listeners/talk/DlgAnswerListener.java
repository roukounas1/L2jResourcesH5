/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripting.scriptengine.listeners.talk;

import l2r.gameserver.model.actor.events.AbstractCharEvents;
import l2r.gameserver.model.actor.events.listeners.IDlgAnswerEventListener;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * @author UnAfraid
 */
public abstract class DlgAnswerListener extends L2JListener implements IDlgAnswerEventListener
{
	private final L2PcInstance _player;
	
	public DlgAnswerListener(L2PcInstance player)
	{
		_player = player;
		register();
	}
	
	@Override
	public void register()
	{
		if (_player == null)
		{
			AbstractCharEvents.registerStaticListener(this);
		}
		else
		{
			_player.getEvents().registerListener(this);
		}
	}
	
	@Override
	public void unregister()
	{
		if (_player == null)
		{
			AbstractCharEvents.unregisterStaticListener(this);
		}
		else
		{
			_player.getEvents().unregisterListener(this);
		}
	}
	
	@Override
	public L2PcInstance getPlayer()
	{
		return _player;
	}
}
