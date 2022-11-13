package common.battle.data;

import com.google.gson.JsonElement;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.Identifier;
import common.util.Data;
import common.util.pack.Soul;
import common.util.unit.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonClass(noTag = NoTag.LOAD)
public abstract class CustomEntity extends DataEntity {

	@JsonField(gen = GenType.GEN)
	public AtkDataModel rep, rev, res, cntr, bur, resu, revi, entr;

	//@JsonField(gen = GenType.GEN) TODO
	//public AtkDataModel[] revs, ress, burs, resus, revis, entrs;

	@JsonField(gen = GenType.GEN)
	public AtkDataModel[] atks;

	//@JsonField(gen = GenType.GEN, usePool = true)
	@JsonField(generic = AtkDataModel[].class, gen = GenType.GEN)
	public ArrayList<AtkDataModel[]> hits = new ArrayList<>();
	@JsonField(gen = GenType.GEN)
	public int[] share;

	public int tba, base, touch = TCH_N;
	public boolean common = true;

	/**
	 * This field is used to filter all the procs of units if common is false,
	 * Also used for counter
	 */

	public CustomEntity() {
		rep = new AtkDataModel(this);
		hits.add(new AtkDataModel[1]);
		hits.get(0)[0] = new AtkDataModel(this);
		share = new int[]{1};
		width = 320;
		speed = 8;
		hb = 1;
		death = new Identifier<>(Identifier.DEF, Soul.class, 0);
	}

	@JsonField(block = true)
	private Proc all;

	@Override
	public int allAtk(int atk) {
		int ans = 0, temp = 0, c = 1;
		for (AtkDataModel adm : hits.get(atk))
			if (adm.pre > 0) {
				ans += temp / c;
				temp = adm.getDire() > 0 ? adm.atk : 0;
				c = 1;
			} else {
				temp += adm.getDire() > 0 ? adm.atk : 0;
				c++;
			}
		ans += temp / c;
		return ans;
	}

	/**
	 * Updates the procs in all and initializes if it is null
	 */
	public void updateAllProc() {
		all = Proc.blank();
		for (int i = 0; i < Data.PROC_TOT; i++) {
			if (Data.procSharable[i]) {
				all.getArr(i).set(getProc().getArr(i));
			} else
				for (AtkDataModel[] adms : hits)
					for (AtkDataModel adm : adms)
						if (!all.getArr(i).exists())
							all.getArr(i).set(adm.proc.getArr(i));
		}
	}

	/**
	 * Gets all procs for units without common proc
	 */
	@Override
	public Proc getAllProc() {
		if (common)
			return getProc();
		if (all == null)
			updateAllProc();
		return all;
	}

	@Override
	public int getAtkCount(int atk) {
		return hits.get(atk).length;
	}

	@Override
	public MaskAtk getAtkModel(int atk, int ind) {
		if (atk >= hits.size() || ind >= hits.get(atk).length)
			return getSpAtks()[ind];
		return hits.get(atk)[ind];
	}

	@Override
	public MaskAtk[] getAtks(int atk) {
		return hits.get(atk);
	}

	@Override
	public int getAtkTypeCount() {
		return hits.size();
	}

	@Override
	public AtkDataModel[][] getAllAtks() {
		return hits.toArray(new AtkDataModel[0][0]);
	}

	public AtkDataModel[] getAllAtkModels() { //Used only on OnInjected for non-shareable procs
		int l = 0;
		AtkDataModel[] sp = getSpAtks();
		for (int i = 0; i < sp.length; i++)
			if (sp[i] != null)
				l++;

		AtkDataModel[] allAtks = new AtkDataModel[hits.get(0).length + 1 + l];
		allAtks[0] = rep;
		System.arraycopy(hits.get(0), 0, allAtks, 1, hits.get(0).length);
		for (int i = 1; i < hits.size(); i++) {
			allAtks = Arrays.copyOf(allAtks, allAtks.length + hits.get(i).length);
			System.arraycopy(hits.get(i), 0, allAtks, allAtks.length - hits.get(i).length, hits.get(i).length);
		}
		final int len = allAtks.length - l;
		l = 0;
		for (int i = 0; i < sp.length; i++)
			if (sp[i] != null)
				allAtks[i + len - l] = sp[i];
			else
				l++;
		return allAtks;
	}

	@Override
	public AtkDataModel[] getSpAtks() {
		return new AtkDataModel[]{rev, res, cntr, bur, resu, revi, entr};
	}

	@Override
	public int getShare(int atk) {
		return share[atk];
	}

	public String getAvailable(String str) {
		while (contains(str))
			str += "'";

		return str;
	}

	@Override
	public int getItv(int atk) {
		int longPre = 0;
		for (AtkDataModel adm : hits.get(atk))
			longPre += adm.pre;
		return longPre + Math.max(getTBA() - 1, getPost(atk));
	}

	@Override
	public int getPost(int atk) {
		int ans = getAnimLen(atk);
		for (AtkDataModel adm : hits.get(atk))
			ans -= adm.pre;
		return ans;
	}

	@Override
	public Proc getProc() {
		return rep.getProc();
	}

	@Override
	public MaskAtk getRepAtk() {
		return rep;
	}

	@Override
	public AtkDataModel getResurrection() {
		return res;
	}

	@Override
	public AtkDataModel getRevenge() {
		return rev;
	}

	@Override
	public AtkDataModel getCounter() { return cntr; }

	@Override
	public AtkDataModel getGouge() {
		return bur;
	}

	@Override
	public AtkDataModel getResurface() {
		return resu;
	}

	@Override
	public AtkDataModel getRevive() {
		return revi;
	}

	@Override
	public AtkDataModel getEntry() {
		return entr;
	}

	@Override
	public int getTBA() {
		return tba;
	}

	@Override
	public int getTouch() {
		return touch;
	}

	public void importData(MaskEntity de) {
		hp = de.getHp();
		hb = de.getHb();
		speed = de.getSpeed();
		range = de.getRange();
		abi = de.getAbi();
		loop = de.getAtkLoop();
		traits = new ArrayList<>();
		for(Trait t : de.getTraits()) {
			if(!t.BCTrait)
				traits.add(t);
			else if(t.id.id != Data.TRAIT_EVA && t.id.id != Data.TRAIT_WITCH)
				traits.add(t);
		}
		width = de.getWidth();
		tba = de.getTBA();
		touch = de.getTouch();
		death = de.getDeathAnim();
		will = de.getWill();
		if (de instanceof CustomEntity) {
			importData$1((CustomEntity) de);
			return;
		}

		base = de.touchBase();
		common = ((DefaultData)de).isCommon();
		rep = new AtkDataModel(this);
		rep.proc = de.getRepAtk().getProc().clone();
		int m = de.getAtkCount(0);
		for (int i = 0; i < m; i++) {
			hits.get(0)[i] = new AtkDataModel(this, de, i);
			for (int j : BCShareable)
				hits.get(0)[i].proc.getArr(j).set(de.getProc().getArr(j));
		}
	}

	@Override
	public boolean isLD() {
		boolean ans = false;
		for (AtkDataModel adm : hits.get(0))
			ans |= adm.isLD();
		for (AtkDataModel adm : getSpAtks())
			if (adm != null)
				ans |= adm.isLD();

		return ans;
	}

	@Override
	public boolean isOmni() {
		boolean ans = false;
		for (AtkDataModel adm : hits.get(0))
			ans |= adm.isOmni();
		for (AtkDataModel adm : getSpAtks())
			if (adm != null)
				ans |= adm.isOmni();

		return ans;
	}

	@Override
	public boolean isRange(int atk) {
		for (AtkDataModel adm : hits.get(atk))
			if (adm.range)
				return true;
		return false;
	}

	@Override
	public int touchBase() {
		return base == 0 ? range : base;
	}

	private boolean contains(String str) {
		if (hits == null || hits.size() == 0)
			return false;
		for (AtkDataModel[] adms : hits)
			for (AtkDataModel adm : adms)
				if (adm != null && adm.str.equals(str))
					return true;
		return false;
	}

	private void importData$1(CustomEntity ce) {
		base = ce.base;
		common = ce.common;
		rep = new AtkDataModel(this, ce.rep);

		List<AtkDataModel> temp = new ArrayList<>();
		List<AtkDataModel> tnew = new ArrayList<>();
		for (int j = 0; j < ce.hits.size(); j++) {
			int[] inds = new int[ce.hits.get(j).length];
			for (int i = 0; i < inds.length; i++) {
				if (!temp.contains(ce.hits.get(j)[i])) {
					temp.add(ce.hits.get(j)[i]);
					tnew.add(new AtkDataModel(this, ce.hits.get(j)[i]));
				}
				inds[i] = temp.indexOf(ce.hits.get(j)[i]);
			}
			hits.set(j, new AtkDataModel[ce.hits.get(j).length]);
			for (int i = 0; i < hits.get(j).length; i++)
				hits.get(j)[i] = tnew.get(inds[i]);
		}
	}

	@Override
	public void animChanged(int del) {
		if (del == -1) {
			hits.add(new AtkDataModel[1]);
			hits.get(hits.size() - 1)[0] = new AtkDataModel(this);
			share = Arrays.copyOf(share, hits.size());
			share[hits.size() - 1] = 1;
		} else {
			hits.remove(del);
			int[] newShare = new int[hits.size()];
			for (int i = 0; i < newShare.length; i++) {
				newShare[i] = share[i < del ? i : i + 1];
			}
			share = newShare;
		}
	}

	@JsonDecoder.OnInjected
	public void onInjected() {
		for (int i = 0; i < traits.size(); i++)
			if (traits.get(i) == null) {
				traits.remove(i);
				i--;
			}
	}
}
