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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.L2ShortCut;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import gr.reunion.interf.ReunionEvents;

/**
 * This class ...
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestShortCutReg extends L2GameClientPacket
{
	private static final String _C__3D_REQUESTSHORTCUTREG = "[C] 3D RequestShortCutReg";
	
	private int _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _lvl;
	private int _characterType; // 1 - player, 2 - pet
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_lvl = readD();
		_characterType = readD();
		
		_slot = slot % 12;
		_page = slot / 12;
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_page > 10) || (_page < 0))
		{
			return;
		}
		
		boolean saveToDb = true;
		
		if (ReunionEvents.isInEvent(activeChar))
		{
			if (!ReunionEvents.canSaveShortcuts(activeChar))
			{
				saveToDb = false;
			}
		}
		
		switch (_type)
		{
			case 0x01: // item
			case 0x02: // skill
			{
				L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
				activeChar.registerShortCut(sc, saveToDb);
				sendPacket(new ShortCutRegister(sc));
				break;
			}
			case 0x03: // action
			case 0x04: // macro
			case 0x05: // recipe
			{
				L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
				activeChar.registerShortCut(sc, saveToDb);
				sendPacket(new ShortCutRegister(sc));
				break;
			}
			case 0x06: // Teleport Bookmark
			{
				L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
				activeChar.registerShortCut(sc, saveToDb);
				sendPacket(new ShortCutRegister(sc));
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__3D_REQUESTSHORTCUTREG;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
