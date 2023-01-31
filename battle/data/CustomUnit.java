package common.battle.data;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.unit.Form;

@JsonClass
public class CustomUnit extends CustomEntity implements MaskUnit, Cloneable {

	public Form pack;

	@JsonField
	public int price, resp, back, front, limit;
	@JsonField(gen = JsonField.GenType.GEN)
	public PCoin pcoin = null;

	public CustomUnit() {
		super();
		hp = 1000;
		price = 50;
		resp = 60;
		back = 0;
		front = 9;
	}

	public CustomUnit(AnimU<?> uni) {
		this();
		share = new int[uni.anim.getAtkCount()];
		share[0] = 1;
		for (int i = hits.size(); i < share.length; i++) {
			hits.add(new AtkDataModel[1]);
			hits.get(i)[0] = new AtkDataModel(this);
			share[i] = 1;
		}
	}

	@Override
	public int getBack() {
		return back;
	}

	@Override
	public int getFront() {
		return front;
	}

	@Override
	public Orb getOrb() {
		return pack.orbs;
	}

	@Override
	public Form getPack() {
		return pack;
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int getRespawn() {
		return resp;
	}

	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public PCoin getPCoin() { return pcoin; }

	@Override
	public void importData(MaskEntity de) {
		super.importData(de);

		if (de instanceof MaskUnit) {
			MaskUnit mu = (MaskUnit) de;

			price = mu.getPrice();
			resp = mu.getRespawn();
			back = Math.min(mu.getBack(), mu.getFront());
			front = Math.max(mu.getBack(), mu.getFront());
			limit = mu.getLimit();

			PCoin p = mu.getPCoin();

			if (p != null) {
				pcoin = new PCoin(this);

				for (int[] i : p.info) {
					int[] j = new int[14];

					System. arraycopy(i, 0, j, 0, 12);

					j[13] = -1;

					j[1] = Math.max(1, j[1]);

					pcoin.info.add(j);
				}
			}
		}
	}

	@Override
	public CustomUnit clone() {
		CustomUnit ans = (CustomUnit) Data.err(super::clone);
		ans.importData(this);
		ans.pack = getPack();
		ans.getPack().anim = getPack().anim;
		return ans;
	}
}
