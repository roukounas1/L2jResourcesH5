package l2f.gameserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fandc.datatables.EnchantNamesTable;
//import fandc.datatables.CharacterMonthlyRanking;
import fandc.datatables.OfflineBuffersTable;
import fandc.facebook.ActionsExtractingManager;
import fandc.facebook.CompletedTasksHistory;
import fandc.facebook.FacebookAutoAnnouncement;
import fandc.facebook.FacebookProfilesHolder;
import fandc.facebook.OfficialPostsHolder;
import fandc.streaming.AFKStreamersHandler;
import fandc.streaming.TwitchParser;
import fandc.tournament.TournamentHolder;
import fandc.votingengine.VotingRewardAPI;
import kara.twitch.TwitchManager;
import kara.vote.VoteManager;
import l2f.commons.listener.Listener;
import l2f.commons.listener.ListenerList;
import l2f.commons.net.AdvIP;
import l2f.commons.net.nio.impl.SelectorThread;
import l2f.commons.versioning.Version;
import l2f.gameserver.cache.CrestCache;
import l2f.gameserver.cache.ImagesCache;
import l2f.gameserver.dao.CharacterDAO;
import l2f.gameserver.dao.EmotionsTable;
import l2f.gameserver.dao.ItemsDAO;
import l2f.gameserver.data.BoatHolder;
import l2f.gameserver.data.xml.Parsers;
import l2f.gameserver.data.xml.holder.EventHolder;
import l2f.gameserver.data.xml.holder.ItemHolder;
import l2f.gameserver.data.xml.holder.ResidenceHolder;
import l2f.gameserver.data.xml.holder.StaticObjectHolder;
import l2f.gameserver.data.xml.parser.ProxiesParser;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.database.LoginDatabaseFactory;
import l2f.gameserver.database.merge.ClanDataMerge;
import l2f.gameserver.database.merge.DataMerge;
import l2f.gameserver.donation.DonationReader;
import l2f.gameserver.geodata.GeoEngine;
import l2f.gameserver.handler.admincommands.AdminCommandHandler;
import l2f.gameserver.handler.items.ItemHandler;
import l2f.gameserver.handler.usercommands.UserCommandHandler;
import l2f.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2f.gameserver.idfactory.IdFactory;
import l2f.gameserver.instancemanager.AutoAnnounce;
import l2f.gameserver.instancemanager.AutoSpawnManager;
import l2f.gameserver.instancemanager.BloodAltarManager;
import l2f.gameserver.instancemanager.CastleManorManager;
import l2f.gameserver.instancemanager.CoupleManager;
import l2f.gameserver.instancemanager.CursedWeaponsManager;
import l2f.gameserver.instancemanager.DimensionalRiftManager;
import l2f.gameserver.instancemanager.HellboundManager;
import l2f.gameserver.instancemanager.L2TopManager;
import l2f.gameserver.instancemanager.PetitionManager;
import l2f.gameserver.instancemanager.PlayerMessageStack;
import l2f.gameserver.instancemanager.RaidBossSpawnManager;
import l2f.gameserver.instancemanager.SoDManager;
import l2f.gameserver.instancemanager.SoIManager;
import l2f.gameserver.instancemanager.SpawnManager;
import l2f.gameserver.instancemanager.achievements_engine.AchievementsManager;
import l2f.gameserver.instancemanager.games.FishingChampionShipManager;
import l2f.gameserver.instancemanager.games.LotteryManager;
import l2f.gameserver.instancemanager.games.MiniGameScoreManager;
import l2f.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2f.gameserver.instancemanager.naia.NaiaCoreManager;
import l2f.gameserver.instancemanager.naia.NaiaTowerManager;
import l2f.gameserver.listener.GameListener;
import l2f.gameserver.listener.game.OnAbortShutdownListener;
import l2f.gameserver.listener.game.OnConfigsReloaded;
import l2f.gameserver.listener.game.OnShutdownCounterStartListener;
import l2f.gameserver.listener.game.OnShutdownListener;
import l2f.gameserver.listener.game.OnStartListener;
import l2f.gameserver.model.PhantomPlayers;
import l2f.gameserver.model.World;
import l2f.gameserver.model.entity.Hero;
import l2f.gameserver.model.entity.MonsterRace;
import l2f.gameserver.model.entity.SevenSigns;
import l2f.gameserver.model.entity.VoteRewardHopzone;
import l2f.gameserver.model.entity.VoteRewardTopzone;
import l2f.gameserver.model.entity.CCPHelpers.itemLogs.ItemLogList;
import l2f.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2f.gameserver.model.entity.achievements.AchievementNotification;
import l2f.gameserver.model.entity.achievements.Achievements;
import l2f.gameserver.model.entity.achievements.PlayerCounters;
import l2f.gameserver.model.entity.auction.AuctionManager;
import l2f.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2f.gameserver.model.entity.forum.ForumDatabaseHandler;
import l2f.gameserver.model.entity.olympiad.Olympiad;
import l2f.gameserver.model.entity.tournament.ActiveBattleManager;
import l2f.gameserver.model.entity.tournament.BattleScheduleManager;
import l2f.gameserver.network.FakeGameClient;
import l2f.gameserver.network.GameClient;
import l2f.gameserver.network.GamePacketHandler;
import l2f.gameserver.network.loginservercon.AuthServerCommunication;
import l2f.gameserver.network.telnet.TelnetServer;
import l2f.gameserver.scripts.Scripts;
import l2f.gameserver.security.HWIDBan;
import l2f.gameserver.tables.AugmentationData;
import l2f.gameserver.tables.ClanTable;
import l2f.gameserver.tables.EnchantHPBonusTable;
import l2f.gameserver.tables.FakePlayersTable;
import l2f.gameserver.tables.FishTable;
import l2f.gameserver.tables.LevelUpTable;
import l2f.gameserver.tables.PetSkillsTable;
import l2f.gameserver.tables.SkillTreeTable;
import l2f.gameserver.taskmanager.BackupTaskManager;
import l2f.gameserver.taskmanager.ItemsAutoDestroy;
import l2f.gameserver.taskmanager.TaskManager;
import l2f.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2f.gameserver.utils.Debug;
import l2f.gameserver.utils.Strings;
import l2f.gameserver.vote.RuVoteEngine;
import l2f.gameserver.vote.VoteMain;
import masteriopack.rankpvpsystem.RPSConfig;
import net.sf.ehcache.CacheManager;

public class GameServer
{
	public static final int AUTH_SERVER_PROTOCOL = 2;
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	public static Date server_started;

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for (Listener<GameServer> listener : getListeners())
			{
				if (OnStartListener.class.isInstance(listener))
				{
					((OnStartListener) listener).onStart();
				}
			}
		}

		public void onShutdown(Shutdown.ShutdownMode shutdownMode)
		{
			for (Listener<GameServer> listener : getListeners())
				if (OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown(shutdownMode);
		}

		public void onAbortShutdown(Shutdown.ShutdownMode oldMode, int cancelledOnSecond)
		{
			for (Listener<GameServer> listener : getListeners())
				if (OnAbortShutdownListener.class.isInstance(listener))
					((OnAbortShutdownListener) listener).onAbortShutdown(oldMode, cancelledOnSecond);
		}

		public void onShutdownScheduled()
		{
			for (Listener<GameServer> listener : getListeners())
				if (OnShutdownCounterStartListener.class.isInstance(listener))
					((OnShutdownCounterStartListener) listener).onCounterStart();
		}

		public void onConfigsReloaded()
		{
			for (Listener<GameServer> listener : getListeners())
				if (OnConfigsReloaded.class.isInstance(listener))
					((OnConfigsReloaded) listener).onConfigsReloaded();
		}
	}

	public static GameServer _instance;

	private final SelectorThread<GameClient> _selectorThreads[];
	private TelnetServer statusServer;
	private final Version version;
	private final GameServerListenerList _listeners;

	private final int _serverStarted;

	public SelectorThread<GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}

	public int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}

	public int uptime()
	{
		return time() - _serverStarted;
	}

	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		_instance = this;
		_serverStarted = time();
		_listeners = new GameServerListenerList();
		
		new File(Config.DATAPACK_ROOT + "/log/").mkdir();

		version = new Version(GameServer.class);

		_log.info("============================================================================================");
		_log.info("Copyright: ............... " + "FandC.ro");
		_log.info("Owner: ................... " + "Ichsan Rinaldi Sjofka");
		_log.info("Edition: ................. " + "Top Premium");
		_log.info("Chronicle: ............... " + "High Five Part 5");
		_log.info("Licensed by: ................... " + "Ichsan Rinaldi Sjofka");
		_log.info("Licensed Admin: ................... " + "Ichsan Rinaldi Sjofka");
		_log.info("Contact Person: ................... " + "+62 8116112333");
		_log.info("Information: ................... " + "Reserved Licensed Owner Auto Grant Admin Privilages");
		_log.info("============================================================================================");

		// Initialize config
		Config.load();
		ConfigHolder.getInstance().reload();
		Debug.initListeners();

		// Check binding address
		checkFreePorts();

//		// We check with internet if the current external ip is the one that is activated for this source, else, we exit the program // license ichsan
//		if (!Config.EXTERNAL_HOSTNAME.equalsIgnoreCase(Patovicador.getInstance().despatovicar(LOCAL_SERVER_IP_ENCRIPTED)))
//		{
//			try
//			{
//				// El URL esta encriptado con la key "xxxYYYxxx" que es la default que cree
//				URL url = new URL(Patovicador.getInstance().despatovicar(WEB_HTML_ENCRIPTED));
//				try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())))
//				{
//					String result = in.readLine();
//					if (result == null || result.isEmpty())
//						throw new Exception();
//
//					// La llave de desencriptacion es la misma IP, por lo que la ip se encripta con llave de ip, y luego esa llave queda para desencriptar el resto de los textos
//					Patovicador.getInstance().setPatollave(result);
//					result = Patovicador.getInstance().patovicar(result);
//
//					// Agregamos soporte para domains
//					String currentIp = SERVER_IP_UNENCRIPTED;
//					try
//					{
//						currentIp = InetAddress.getByName(currentIp).getHostAddress();
//					}
//					catch (Exception e) {}
//
//					if (!result.equalsIgnoreCase(Patovicador.getInstance().patovicar(currentIp)))
//						throw new Exception();
//				}
//			}
//			catch (Exception e)
//			{
//				System.out.println("Wrong License");
//				System.exit(1);
//			}
//		}

		// Initialize database
		_log.info("Licensed IP " + Config.EXTERNAL_HOSTNAME );
		_log.info("Licensed IP " + "Please Contact Ichsan Rinaldi Sjofka");

		Class.forName(Config.DATABASE_DRIVER).newInstance();
		DatabaseFactory.getInstance().getConnection().close();
		LoginDatabaseFactory.getInstance().getConnection().close();
		printSection("Loading Protection Configuration");
		IdFactory idFactory = IdFactory.getInstance();
		if (!idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.", new Exception("Could not initialize the ID factory"));
			throw new Exception("Could not initialize the ID factory");
		}

		CacheManager.getInstance();

		ThreadPoolManager.getInstance();

		_log.info("===============[Loading Scripts]==================");
		Scripts.getInstance();
		GeoEngine.load();
		VoteMain.load();
		printSection("Twitch Manager");
		TwitchManager.getInstance();
		printSection("Vote Manager");
		VoteManager.getInstance();
		// FakePlayers.getInstance();
		FakePlayersTable.getInstance();
		Strings.reload();
		GameTimeController.getInstance();
		printSection("Lineage World");
		World.init();
		printSection("");
		Parsers.parseAll();
		printSection("Banned HWIDS");
		HWIDBan.LoadAllHWID();
		ItemsDAO.getInstance();
		printSection("Clan Crests");
		CrestCache.getInstance();
		printSection("Loading Images");
		ImagesCache.getInstance();
		printSection("");
		CharacterDAO.getInstance();
		ClanTable.getInstance();
		printSection("Fish Table");
		FishTable.getInstance();
		printSection("Skills");
		SkillTreeTable.getInstance();
		EnchantNamesTable.getInstance();
		printSection("Augmentation Data");
		AugmentationData.getInstance();
		EnchantHPBonusTable.getInstance();
		printSection("Level Up Table");
		LevelUpTable.getInstance();
		PetSkillsTable.getInstance();
		printSection("Item Logs");
		ItemLogList.getInstance().loadAllLogs();
		printSection("Auctioneer");
		ItemAuctionManager.getInstance();
		printSection("Masterio Pack");
		RPSConfig.load();
		printSection("Merge System Loaded");
		DataMerge.getInstance();
		ClanDataMerge.getInstance();
		Scripts.getInstance().init();
		_log.info("===============[Spawn Manager]==================");
		SpawnManager.getInstance().spawnAll();
		printSection("Boats");
		BoatHolder.getInstance().spawnAll();
		StaticObjectHolder.getInstance().spawnAll();
		RaidBossSpawnManager.getInstance();
		printSection("Dimensional Rift");
		DimensionalRiftManager.getInstance();
		Announcements.getInstance();
		LotteryManager.getInstance();
		PlayerMessageStack.getInstance();
		if (Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			ItemsAutoDestroy.getInstance();
		}
		MonsterRace.getInstance();
		printSection("Seven Signs");
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().updateFestivalScore();
		AutoSpawnManager.getInstance();
		SevenSigns.getInstance().spawnSevenSignsNPC();
		_log.info("===================================================================");
		_log.info("===================[Loading Olympiad System]=======================");
		if (Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}
		_log.info("===================[Olympiad System Loaded]=======================");
		_log.info("===================================================================");
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		printSection("Loaded Small Achievement System");
		AchievementsManager.getInstance();
		if (!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}
		ItemHandler.getInstance();
		printSection("Admin Commands");
		AdminCommandHandler.getInstance().log();
		printSection("Players Commands");
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		TaskManager.getInstance();
		_log.info("======================[Tournament By Kara`]==========================");
		TournamentHolder.init();
		_log.info("======================[Loading Castels & Clan Halls]==========================");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		CastleManorManager.getInstance();
		printSection("");
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		printSection("Auto Cleaner");
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		printSection("");
		CoupleManager.getInstance();
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionShipManager.getInstance();
		}
		printSection("Hellbound");
		HellboundManager.getInstance();

		NaiaTowerManager.getInstance();
		NaiaCoreManager.getInstance();
		printSection("");
		SoDManager.getInstance();
		SoIManager.getInstance();
		BloodAltarManager.getInstance();
		AuctionManager.getInstance();
		if (Config.ALLOW_DROP_CALCULATOR)
		{
			_log.info("Preparing Drop Calculator");
			ItemHolder.getInstance().getDroppableTemplates();
		}
		MiniGameScoreManager.getInstance();
		if (Config.ALLOW_HOPZONE_VOTE_REWARD)
		{
			VoteRewardHopzone.getInstance();
		}
		if (Config.ALLOW_TOPZONE_VOTE_REWARD)
		{
			VoteRewardTopzone.getInstance();
		}
		L2TopManager.getInstance();
		//AutoRaidEventManager.getInstance();
		if (Config.BUFF_STORE_ENABLED)
		{
			printSection("Offline Buffers");
			OfflineBuffersTable.getInstance().restoreOfflineBuffers();
		}

		if (Config.ENABLE_PLAYER_COUNTERS)
		{
			PlayerCounters.checkTable();
			AchievementNotification.getInstance();

			if (Config.ENABLE_ACHIEVEMENTS)
			{
				Achievements.getInstance();
			}
		}
		if (Config.ENABLE_EMOTIONS)
		{
			EmotionsTable.init();
			_log.info("Emotions Loaded....");
		}

		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.ShutdownMode.RESTART, Config.BACKUP_DURING_AUTO_RESTART);
		printSection("");
		_log.info(">>>>>>>>>>>>>>> GameServer Started <<<<<<<<<<<<<<");
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

		CharacterDAO.getInstance().markTooOldChars();
		printSection("DataBase Cleaner Loaded");
		CharacterDAO.getInstance().checkCharactersToDelete();
		FightClubEventManager.getInstance();
		BattleScheduleManager.getInstance();
		ActiveBattleManager.startScheduleThread();

		GamePacketHandler gph = new GamePacketHandler();
		FakeGameClient.setGamePacketHandler(gph);
		InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
		int arrayLen = Config.GAMEIPS.isEmpty() ? Config.PORTS_GAME.length : Config.PORTS_GAME.length + Config.GAMEIPS.size();
		_selectorThreads = new SelectorThread[arrayLen];
		for (int i = 0; i < Config.PORTS_GAME.length; i++)
		{
			try
			{
				_selectorThreads[i] = new SelectorThread<GameClient>(Config.SELECTOR_CONFIG, gph, gph, gph, null);
				_selectorThreads[i].openServerSocket(serverAddr, Config.PORTS_GAME[i]);
				_selectorThreads[i].start();
			}
			catch (IOException ioe)
			{
				_log.error("Cannot bind address: " + serverAddr + ":" + Config.PORTS_GAME[i], ioe);
			}
		}
		if (!Config.GAMEIPS.isEmpty()) // AdvIP support. server.ini ports are ignored and accepted only IPs and ports from advipsystem.ini
		{
			int i = Config.PORTS_GAME.length; // Start from the last spot.
			for (AdvIP advip : Config.GAMEIPS)
			{
				try
				{
					_selectorThreads[i] = new SelectorThread<GameClient>(Config.SELECTOR_CONFIG, gph, gph, gph, null);
					_selectorThreads[i].openServerSocket(InetAddress.getByName(advip.channelAdress), advip.channelPort);
					_selectorThreads[i++].start();
					_log.info("AdvIP: Channel " + advip.channelId + " is open on: " + advip.channelAdress + ":" + advip.channelPort);
				}
				catch (IOException ioe)
				{
					_log.error("Cannot bind address: " + advip.channelAdress + ":" + advip.channelPort, ioe);
				}
			}
		}

		if (Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
		{
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 100000L);
		}
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoAnnounce(), 60000, 60000);
		FacebookProfilesHolder.getInstance();
		printSection("Loaded Facebook System");
		OfficialPostsHolder.getInstance();
		CompletedTasksHistory.getInstance();
		ActionsExtractingManager.getInstance().load();
		FacebookAutoAnnouncement.load();
		if (ConfigHolder.getBool("AllowStreamingAFKSystem") && ConfigHolder.getInt("StreamingAFKSystemDelayBetweenMsgs") > 0)
		{
			AFKStreamersHandler.getInstance();
			printSection("Loaded Stream System");
		}

		if (ConfigHolder.getBool("AllowStreamingSystem") && ConfigHolder.getLong("StreamCheckTwitchDelay") > 0)
			TwitchParser.getInstance();

		if (ConfigHolder.getBool("AllowForum"))
		{
			_log.info("===============[Forum]==================");
			ForumDatabaseHandler.getInstance();
		}
		ProxiesParser.getInstance().load();
		printSection("Loaded Proxy System");
		DonationReader.getInstance();
		printSection("Loaded AUTO - Donation System");
		RuVoteEngine.startThread();
		BackupTaskManager.startThread();

		VotingRewardAPI.getInstance();

		if (Config.PHANTOM_PLAYERS_ENABLED)
			PhantomPlayers.init();

		getListeners().onStart();
		if (Config.IS_TELNET_ENABLED)
		{
			statusServer = new TelnetServer();
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}

		AuthServerCommunication.getInstance().start();
		server_started = new Date();
	}

	public static void printSection(String s)
	{
		if (s.isEmpty())
		{
			s = "==============================================================================";
		}
		else
		{
			s = "=[ " + s + " ]";
			while (s.length() < 78)
			{
				s = "-" + s;
			}
		}
		_log.info(s);
	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return _instance;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}

	public static void checkFreePorts()
	{
		boolean binded = false;
		while (!binded)
		{
			for (int PORT_GAME : Config.PORTS_GAME)
			{
				try
				{
					ServerSocket ss;
					if (Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
					{
						ss = new ServerSocket(PORT_GAME);
					}
					else
					{
						ss = new ServerSocket(PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
					}
					ss.close();
					binded = true;
				}
				catch (Exception e)
				{
					_log.warn("Port " + PORT_GAME + " is allready binded. Please free it and restart server.");
					binded = false;
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e2)
					{
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		new GameServer();
	}

	public Version getVersion()
	{
		return version;
	}

	public TelnetServer getStatusServer()
	{
		return statusServer;
	}

//	private static String WEB_HTML_ENCRIPTED;
//	static
//	{
//		WEB_HTML_ENCRIPTED = "qR3LAktic6T7TCzrXxXKzZ1+7pPOFjyBhwnwQEw25mw=";
//		// Esto sale de la direccion http://checkip.amazonaws.com, pero encriptandolo con la key original
//	}
//
//	private static String SERVER_IP_UNENCRIPTED;
//	static
//	{
//		SERVER_IP_UNENCRIPTED = "127.0.0.1";
//		// Esta es la ip registrada al sistema, no encriptada, para quitarle un poco de dificultad
//	}
//
//	private static String LOCAL_SERVER_IP_ENCRIPTED;
//	static
//	{
//		LOCAL_SERVER_IP_ENCRIPTED = "h8slj0HGUCLCX7LEhbggaw==";
//		// Vendria a ser 127.0.0.1, usada para evitar chequeos cuando se corre el server en la pc propia. Encriptada con la key original
//	}
}