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
package l2r.gameserver.scripting;

import java.io.File;

import javax.script.ScriptException;

/**
 * Abstract class for classes that are meant to be implemented by scripts.<BR>
 * @author KenM
 */
public abstract class ManagedScript
{
	private final File _scriptFile;
	private long _lastLoadTime;
	private boolean _isActive;
	
	public ManagedScript()
	{
		_scriptFile = L2ScriptEngineManager.getInstance().getCurrentLoadingScript();
		setLastLoadTime(System.currentTimeMillis());
	}
	
	/**
	 * Attempts to reload this script and to refresh the necessary bindings with it ScriptControler.<BR>
	 * Subclasses of this class should override this method to properly refresh their bindings when necessary.
	 * @return true if and only if the script was reloaded, false otherwise.
	 */
	public boolean reload()
	{
		try
		{
			L2ScriptEngineManager.getInstance().executeScript(getScriptFile());
			return true;
		}
		catch (ScriptException e)
		{
			return false;
		}
	}
	
	public abstract boolean unload();
	
	public void setActive(boolean status)
	{
		_isActive = status;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	/**
	 * @return Returns the scriptFile.
	 */
	public File getScriptFile()
	{
		return _scriptFile;
	}
	
	/**
	 * @param lastLoadTime The lastLoadTime to set.
	 */
	protected void setLastLoadTime(long lastLoadTime)
	{
		_lastLoadTime = lastLoadTime;
	}
	
	/**
	 * @return Returns the lastLoadTime.
	 */
	protected long getLastLoadTime()
	{
		return _lastLoadTime;
	}
	
	public abstract String getScriptName();
	
	public abstract ScriptManager<?> getScriptManager();
}
