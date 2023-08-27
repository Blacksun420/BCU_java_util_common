package common.battle.attack;

import common.CommonStatic;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.util.pack.EffAnim.DefEff;

import java.util.HashSet;
import java.util.Set;

public class ContWaveDef extends ContWaveAb {

	protected ContWaveDef(AttackWave a, double p, int layer, boolean delay) {
		this(a, p, layer, delay, new HashSet<>());
	}

	protected ContWaveDef(AttackWave a, double p, int layer, boolean delay, Set<ContWaveAb> waves) {
		super(a, p, (a.dire == 1 ? a.waveType == WT_MEGA ? effas().A_E_MEGAWAVE : a.waveType == WT_MINI ? effas().A_E_MINIWAVE : effas().A_E_WAVE
				: a.waveType == WT_MEGA ? effas().A_MEGAWAVE : a.waveType == WT_MINI ? effas().A_MINIWAVE : effas().A_WAVE).getEAnim(DefEff.DEF), layer, delay);
		soundEffect = SE_WAVE;

		maxt -= 1;
		anim.setTime(1);
		this.waves = waves;
		this.waves.add(this);
	}

	@Override
	public void update() {
		tempAtk = false;
		boolean isMini = atk.waveType == WT_MINI, isMega = atk.waveType == WT_MEGA;
		// guessed attack point compared from BC
		int attack = (isMini || isMega ? 4 : 6);
		// guessed wave block time compared from BC
		if (t == 0)
			CommonStatic.setSE(soundEffect);
		if (t >= (isMini ? 1 : 2) && t <= attack) {
			atk.capture();
			for (AbEntity e : atk.capt)
				if (e instanceof Entity) {
					int waves = e.getProc().IMUWAVE.block;
					if (waves != 0) {
						if (waves > 0)
							((Entity)e).anim.getEff(STPWAVE);
						if (waves == 100) {
							deactivate();
							return;
						} else
							atk.raw = atk.raw * (100 - waves) / 100;
					}
				}
		}
		if (!activate)
			return;
		if (t == (isMega ? W_MEGA_TIME : isMini ? W_MINI_TIME : W_TIME)) {
			if ((isMini || isMega) && atk.proc.MINIWAVE.lv > this.waves.size())
				nextWave();
			else if (!(isMini || isMega) && atk.proc.WAVE.lv > this.waves.size())
				nextWave();
		}
		if (t == attack) {
			sb.getAttack(atk);
			tempAtk = true;
		}
		if (maxt == t)
			activate = false;
		updateAnimation();
		t++;
	}

	@Override
	public void updateAnimation() {
		if (t >= 0)
			anim.update(false);
	}

	@Override
	protected void nextWave() {
		int dire = atk.model.getDire();
		double np = pos + W_PROG * dire;
		int wid = dire == 1 ? W_E_WID : W_U_WID;
		new ContWaveDef(new AttackWave(atk.attacker, atk, np, wid), np, layer, false, waves);
	}

	@Override
	public boolean IMUTime() {
		return atk.attacker != null && (atk.attacker.getAbi() & AB_TIMEI) != 0;
	}
}
