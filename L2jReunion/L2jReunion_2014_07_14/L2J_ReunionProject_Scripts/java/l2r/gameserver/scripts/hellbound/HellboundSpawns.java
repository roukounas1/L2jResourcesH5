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
package l2r.gameserver.scripts.hellbound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Hellbound Spawns parser.
 * @author Zoey76
 */
public final class HellboundSpawns extends DocumentParser
{
	private final List<L2Spawn> _spawns = new ArrayList<>();
	private final Map<Integer, int[]> _spawnLevels = new HashMap<>();
	
	public HellboundSpawns()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_spawns.clear();
		_spawnLevels.clear();
		parseDatapackFile("data/scripts/hellbound/hellboundSpawns.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _spawns.size() + " Hellbound spawns.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node node = getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equals(node.getNodeName()))
			{
				for (Node npc = node.getFirstChild(); npc != null; npc = npc.getNextSibling())
				{
					parseSpawn(npc);
				}
			}
		}
	}
	
	/**
	 * Parses the spawn.
	 * @param npc the NPC to parse
	 */
	private void parseSpawn(Node npc)
	{
		if ("npc".equals(npc.getNodeName()))
		{
			final Node id = npc.getAttributes().getNamedItem("id");
			if (id == null)
			{
				_log.error(getClass().getSimpleName() + ":  Missing NPC ID, skipping record!");
				return;
			}
			
			final int npcId = Integer.parseInt(id.getNodeValue());
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template == null)
			{
				_log.warn(getClass().getSimpleName() + ": Missing NPC template for ID: " + npcId + "!");
				return;
			}
			
			Location loc = null;
			int delay = 0;
			int randomInterval = 0;
			int minLevel = 1;
			int maxLevel = 100;
			for (Node element = npc.getFirstChild(); element != null; element = element.getNextSibling())
			{
				final NamedNodeMap attrs = element.getAttributes();
				minLevel = 1;
				maxLevel = 100;
				switch (element.getNodeName())
				{
					case "location":
					{
						loc = new Location(parseInteger(attrs, "x"), parseInteger(attrs, "y"), parseInteger(attrs, "z"), parseInteger(attrs, "heading", 0));
						break;
					}
					case "respawn":
					{
						delay = parseInteger(attrs, "delay");
						randomInterval = attrs.getNamedItem("randomInterval") != null ? parseInteger(attrs, "randomInterval") : 1;
						break;
					}
					case "hellboundLevel":
					{
						minLevel = parseInteger(attrs, "min", 1);
						maxLevel = parseInteger(attrs, "max", 100);
						break;
					}
				}
			}
			
			try
			{
				final L2Spawn spawn = new L2Spawn(template);
				spawn.setAmount(1);
				if (loc == null)
				{
					_log.warn("location is null");
				}
				spawn.setLocation(loc);
				spawn.setRespawnDelay(delay, randomInterval);
				_spawnLevels.put(npcId, new int[]
				{
					minLevel,
					maxLevel
				});
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				_spawns.add(spawn);
			}
			catch (SecurityException | ClassNotFoundException | NoSuchMethodException e)
			{
				_log.warn(getClass().getSimpleName() + ": Couldn't load spawns: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Gets all Hellbound spawns.
	 * @return the list of Hellbound spawns.
	 */
	public List<L2Spawn> getSpawns()
	{
		return _spawns;
	}
	
	/**
	 * Gets the spawn minimum level.
	 * @param npcId the NPC ID
	 * @return the spawn minimum level
	 */
	public int getSpawnMinLevel(int npcId)
	{
		return _spawnLevels.containsKey(npcId) ? _spawnLevels.get(npcId)[0] : 1;
	}
	
	/**
	 * Gets the spawn maximum level.
	 * @param npcId the NPC ID
	 * @return the spawn maximum level
	 */
	public int getSpawnMaxLevel(int npcId)
	{
		return _spawnLevels.containsKey(npcId) ? _spawnLevels.get(npcId)[1] : 1;
	}
	
	public static HellboundSpawns getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HellboundSpawns INSTANCE = new HellboundSpawns();
	}
}