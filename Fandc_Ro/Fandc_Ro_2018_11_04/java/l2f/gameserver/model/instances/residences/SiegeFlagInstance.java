package l2f.gameserver.model.instances.residences;

import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.model.entity.events.objects.SiegeClanObject;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.model.pledge.Clan;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.templates.npc.NpcTemplate;

import org.apache.commons.lang3.StringUtils;

public class SiegeFlagInstance extends NpcInstance
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SiegeClanObject _owner;
	private long _lastAnnouncedAttackedTime = 0;

	public SiegeFlagInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public String getName()
	{
		return _owner.getClan().getName();
	}

	@Override
	public Clan getClan()
	{
		return _owner.getClan();
	}

	@Override
	public String getTitle()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if (player == null || isInvul())
			return false;
		Clan clan = player.getClan();
		return clan == null || _owner.getClan() != clan;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		_owner.setFlag(null);
		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if (System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000)
		{
			_lastAnnouncedAttackedTime = System.currentTimeMillis();
			_owner.getClan().broadcastToOnlineMembers(SystemMsg.YOUR_BASE_IS_BEING_ATTACKED);
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean isHealBlocked()
	{
		return true;
	}

	@Override
	public boolean isEffectImmune()
	{
		return true;
	}

	public void setClan(SiegeClanObject owner)
	{
		_owner = owner;
	}
}