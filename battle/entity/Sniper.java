package common.battle.entity;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.attack.AtkModelAb;
import common.battle.attack.AttackAb;
import common.battle.attack.AttackSimple;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.Data;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim.SniperEff;
import common.util.unit.Trait;

import java.util.ArrayList;

public class Sniper extends AtkModelAb {

	private final EAnimD<?> anim = effas().A_SNIPER.getEAnim(SniperEff.IDLE);
	private final EAnimD<?> atka = effas().A_SNIPER.getEAnim(SniperEff.ATK);
	private int coolTime = SNIPER_CD, preTime = 0, atkTime = 0;
	//private P path;
	public boolean enabled = true, canDo = true;
	public double pos, height, bulletX,cannonAngle = 0, bulletAngle = 0;

	public Sniper(StageBasis sb) {
		super(sb);
	}

	/**
	 * base part of animation
	 */
	public void drawBase(FakeGraphics gra, P ori, double siz) {
		height = ori.y;

		if (atkTime == 0)
			anim.draw(gra, ori, siz);
		else {
			atka.draw(gra, ori, siz);
		}
	}

	@Override
	public int getAbi() {
		return 0;
	}

	@Override
	public int getDire() {
		return -1;
	}

	@Override
	public double getPos() {
		return b.ubase.pos + SNIPER_POS;
	}

	private void getAngle() {
		double Cx = b.st.len - 225;
		double Uy = 280;
		double Cy = 275;
		double r = b.pos / CommonStatic.BattleConst.ratio;

		if(bulletX == 0) {
			bulletAngle = Math.atan2((2.5 * Math.sin(Math.PI / 30 * b.time) + (int) (Cy / 10) - 92.25) - (int) (Uy / 10) + 14.5, (int) ((Cx - r) / 10) + 50.75 - (int) ((pos - r) / 10));
		}

		//Formula is different, only for visual
		cannonAngle = Math.atan2((2.5 * Math.sin(Math.PI / 30 * b.time) + (int) (Cy / 10) - 92.25) - (int) (Uy / 10) + 14.5, (int) ((Cx - r) / 10) - 50.75 - (int) ((pos - r) / 10));
	}

	public void update() {
		if (canDo && b.ubase.health <= 0) {
			canDo = false;
		}

		if (enabled && coolTime > 0)
			coolTime--;

		if (coolTime == 0 && enabled && pos > 0 && canDo) {
			coolTime = SNIPER_CD;
			preTime = SNIPER_PRE;
			atkTime = atka.len();
			atka.setup();
			anim.setup();
		}

		// find enemy pos
		pos = -1;
		for (Entity e : b.le)
			if (e.dire == 1 && e.pos > pos && !e.isBase && (e.touchable() & TCH_N) > 0)
				pos = e.pos;

		getAngle();

		if (preTime > 0) {
			preTime--;
			if (preTime == 0) {
				//fire bullet
				bulletX = b.ubase.pos + SNIPER_POS + ((int) (1500 * Math.cos(bulletAngle))) / 4.0;

				atka.ent[6].alter(12, 1000);
				anim.ent[6].alter(12, 1000);
			}
		}

		if (bulletX != 0 && bulletX > pos) {
			bulletX -= ((int) (1500 * Math.cos(bulletAngle))) / 4.0;

			atka.ent[6].alter(4, (int) ((bulletX - b.ubase.pos - SNIPER_POS) / Math.cos(bulletAngle) * CommonStatic.BattleConst.ratio * 1.13));
			anim.ent[6].alter(4, (int) ((bulletX - b.ubase.pos - SNIPER_POS) / Math.cos(bulletAngle) * CommonStatic.BattleConst.ratio * 1.13));

			if (bulletX <= pos) {
				int atk = b.b.t().getBaseHealth() / 20;
				Proc proc = Proc.blank();
				proc.SNIPER.prob = 1;
				ArrayList<Trait> CTrait = new ArrayList<>();
				CTrait.add(UserProfile.getBCData().traits.get(TRAIT_TOT));
				AttackAb a = new AttackSimple(null, this, atk, CTrait, 0, proc, 0, getPos(), false, null, -1, true, 1);
				a.canon = -1;
				b.getAttack(a);

				bulletX = 0;

				atka.ent[6].alter(12, 0);
				anim.ent[6].alter(12, 0);
			}
		}

		if (atkTime > 0) {
			atkTime--;
			atka.update(false);
		} else {
			anim.update(true);
		}

		if(bulletX > 0) {
			anim.ent[6].alter(12, 1000);
			anim.ent[5].alter(11, (int) Math.round(bulletAngle * 180 / Math.PI * 10));
		} else {
			anim.ent[5].alter(11, (int) Math.round(cannonAngle * 180 / Math.PI * 10));
		}

		atka.ent[5].alter(11, (int) Math.round(bulletAngle * 180 / Math.PI * 10));

		anim.ent[1].alter(5, - (int) Math.round((989.5 + 25 * Math.sin(Math.PI * b.time / 30) - height) * CommonStatic.BattleConst.ratio));
		atka.ent[1].alter(5, - (int) Math.round((989.5 + 25 * Math.sin(Math.PI * b.time / 30) - height) * CommonStatic.BattleConst.ratio));
	}
}
