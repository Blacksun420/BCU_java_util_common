package common.battle.data;

@SuppressWarnings("ForLoopReplaceableByForEach")
public abstract class DefaultData extends DataEntity {

	public Proc proc;
	protected int[] lds = new int[1], ldr = new int[1];
	protected int atk, atk1, atk2, pre, pre1, pre2, abi0 = 1, abi1, abi2, tba;
	protected DataAtk[] datks;

	public boolean isrange;

	@Override
	public int allAtk() {
		return atk + atk1 + atk2;
	}

	@Override
	public Proc getAllProc() {
		return proc;
	}

	@Override
	public int getAtkCount() {
		return atk1 == 0 ? 1 : atk2 == 0 ? 2 : 3;
	}

	@Override
	public MaskAtk getAtkModel(int ind) {
		if (ind >= getAtkCount() || datks == null || ind >= datks.length)
			return null;
		return datks[ind];
	}

	@Override
	public MaskAtk[] getAtks() {
		return datks;
	}

	@Override
	public int getItv() {
		return getLongPre() + Math.max(getTBA() - 1, getPost());
	}

	@Override
	public int getPost() {
		return getAnimLen() - getLongPre();
	}

	@Override
	public Proc getProc() {
		return proc;
	}

	@Override
	public MaskAtk getRepAtk() {
		return datks[0];
	}

	@Override
	public int getTBA() {
		return tba * 2;
	}

	@Override
	public boolean isLD() {
		for(int i = 0; i < ldr.length; i++) {
			if(ldr[i] <= 0)
				return false;
		}

		return true;
	}

	@Override
	public boolean isOmni() {
		for(int i = 0; i < ldr.length; i++) {
			if(ldr[i] < 0)
				return true;
		}

		return false;
	}

	@Override
	public boolean isRange() {
		return isrange;
	}

	@Override
	public int[][] rawAtkData() {
		int[][] data = new int[getAtkCount()][4];
		data[0][0] = atk;
		data[0][1] = pre;
		data[0][2] = abi0;
		data[0][3] = 1;
		if (atk1 == 0)
			return data;
		data[1][0] = atk1;
		data[1][1] = pre1 - pre;
		data[1][2] = abi1;
		data[1][3] = 1;
		if (atk2 == 0)
			return data;
		data[2][0] = atk2;
		data[2][1] = pre2 - pre1;
		data[2][2] = abi2;
		data[2][3] = 1;
		return data;
	}

	@Override
	public int touchBase() {
		return lds[0] > 0 ? lds[0] : range;
	}

	protected int getLongPre() {
		if (pre2 > 0)
			return pre2;
		if (pre1 > 0)
			return pre1;
		return pre;
	}

	public boolean isCommon() {
		for (int[] atkDatum : rawAtkData()) {
			if (atkDatum[2] != 1)
				return false;
		}
		return true;
	}

}
