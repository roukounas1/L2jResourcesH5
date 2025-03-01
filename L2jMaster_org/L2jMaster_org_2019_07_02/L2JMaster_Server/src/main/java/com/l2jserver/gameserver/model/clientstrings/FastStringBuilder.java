/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.clientstrings;

/**
 * @author Forsaiken
 */
final class FastStringBuilder
{
	private final char[] _array;
	private int _len;
	
	public FastStringBuilder(final int capacity)
	{
		_array = new char[capacity];
	}
	
	public final void append(final String text)
	{
		text.getChars(0, text.length(), _array, _len);
		_len += text.length();
	}
	
	@Override
	public final String toString()
	{
		return new String(_array);
	}
}