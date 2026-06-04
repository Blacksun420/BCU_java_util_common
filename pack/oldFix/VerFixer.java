package common.pack.oldFix;

import com.google.common.io.Files;
import common.CommonStatic;
import common.battle.data.CustomEnemy;
import common.battle.data.CustomUnit;
import common.io.InStream;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.Identifier;
import common.pack.PackData.PackDesc;
import common.pack.PackData.UserPack;
import common.pack.Source;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.ImageBuilder;
import common.system.files.FDFile;
import common.system.files.FileData;
import common.util.Data;
import common.util.Data.Proc.REVIVE;
import common.util.Data.Proc.POISON;
import common.util.Data.Proc.SPEED;
import common.util.anim.AnimCE;
import common.util.anim.AnimCI;
import common.util.pack.Background;
import common.util.stage.CastleImg;
import common.util.stage.MapColc.PackMapColc;
import common.util.stage.Music;
import common.util.unit.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static common.pack.Source.SourceAnimLoader.*;

@SuppressWarnings("deprecation")
public abstract class VerFixer extends Source {//TODO: Use VER FIXER to fix even older bcupack files

    private static final String ID_FIXER = "id_fixer";

    public static class IdFixer {

        private final Class<?> ent;

        public IdFixer(Class<?> cls) {
            ent = cls == null ? AbEnemy.class : cls;
        }

        public Class<?> parse(int val, Class<?> cls) {
            if (cls == Data.Proc.THEME.class)
                return Background.class;
            else if (ent == Unit.class)
                return ent;
            else
                return val % 1000 < 500 ? Enemy.class : EneRand.class;
        }
    }

    public interface ImgReader {

        static File loadMusicFile(ISStream is, ImgReader r, int pid, int mid) {
            if (r == null || r.isNull())
                r = new MusicReader(pid, mid);
            return r.readFile(is);
        }

        static VImg readImg(ISStream is, ImgReader r) {
            if (r != null && !r.isNull())
                return r.readImgOptional(is.nextString());
            return ImageBuilder.toVImg(is.nextBytesI());
        }

        default boolean isNull() {
            return true;
        }

        File readFile(ISStream is);

        FakeImage readImg(String str);

        VImg readImgOptional(String str);
    }

    private static class MusicReader implements ImgReader {

        private final int pid, mid;

        private MusicReader(int p, int m) {
            pid = p;
            mid = m;
        }

        @Override
        @Deprecated
        public File readFile(ISStream is) {
            byte[] bs = is.subStream().nextBytesI();
            String path = "./pack/music/" + Data.hex(pid) + "/" + Data.trio(mid) + ".ogg";
            File f = CommonStatic.ctx.getAuxFile(path);
            Data.err(() -> Context.check(f));
            try {
                Files.write(bs, f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return f;
        }

        @Override
        public FakeImage readImg(String str) {
            return null;
        }

        @Override
        public VImg readImgOptional(String str) {
            return null;
        }

    }

    public static class VerFixerException extends Exception {

        private static final long serialVersionUID = 1L;

        public VerFixerException(String str) {
            super(str);
        }

    }

    @Deprecated
    private static class PackFixer extends VerFixer {

        private final ImgReader r;

        public PackFixer(String id, ImgReader r) {
            super(id);
            this.r = r;
        }

        @Deprecated
        @Override
        protected void load() throws Exception {
            data.desc.names.put(is.nextString());
            loadEnemies(is.subStream());
            loadUnits(is.subStream());
            loadCastles(is.subStream());
            loadBackgrounds(is.subStream());
            loadMusics(is.subStream());
            data.mc = new PackMapColc(data, is);
            is.close();
            is = null;
        }

        private void loadBackgrounds(ISStream is) throws Exception {
            int ver = Data.getVer(is.nextString());
            if (ver != 400)
                throw new VerFixerException("expect bg store version to be 400, got " + ver);
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int ind = is.nextInt();
                VImg vimg = ImgReader.readImg(is, r);
                vimg.name = Data.trio(ind);
                writeImgs(vimg, "backgrounds", vimg.name + ".png");
                Background bg = new Background(new Identifier<>(id, Background.class, ind), vimg);
                data.bgs.set(ind, bg);
                bg.top = is.nextInt() > 0;
                bg.ic = is.nextInt();
                for (int j = 0; j < 4; j++) {
                    int p = is.nextInt();
                    bg.cs[j] = new int[] { p >> 8 & 255, p >> 8 & 255, p & 255 };
                }
            }
        }

        private void loadCastles(ISStream is) throws Exception {
            int ver = Data.getVer(is.nextString());
            if (ver != 307)
                throw new VerFixerException("expect castle store version to be 307, got " + ver);
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int val = is.nextInt();
                VImg vimg = ImgReader.readImg(is, r);
                vimg.name = Data.trio(val);
                writeImgs(vimg, "castles", vimg.name + ".png");
                data.castles.set(val, new CastleImg(new Identifier<>(id, CastleImg.class, val), vimg));
            }
        }

        private void loadEnemies(ISStream is) throws VerFixerException {
            int ver = Data.getVer(is.nextString());
            if (ver != 402)
                throw new VerFixerException("expect enemy store version to be 402, got " + ver);
            UserProfile.setStatic(ID_FIXER, new IdFixer(AbEnemy.class));
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int hash = is.nextInt();
                String str = is.nextString();
                CustomEnemy ce = new CustomEnemy();
                ce.convertOldData(Data.getVer(is.nextString()), is);
                AnimCE ac = decodeAnim(".temp_" + id, is.subStream(), r);
                Enemy e = new Enemy(new Identifier<>(id, Enemy.class, hash % 1000), ac, ce);
                e.names.put(str);
                data.enemies.set(hash % 1000, e);
            }
            n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int hash = is.nextInt();
                EneRand e = new EneRand(new Identifier<>(id, EneRand.class, hash % 1000));
                e.convertOldData(is.subStream());
                data.randEnemies.set(hash % 1000, e);
            }
        }

        private void loadMusics(ISStream is) throws VerFixerException {
            int ver = Data.getVer(is.nextString());
            if (ver != 307)
                throw new VerFixerException("expect music store version to be 307, got " + ver);
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int val = is.nextInt();
                File f = ImgReader.loadMusicFile(is, r, Integer.parseInt(id), val);
                File fx = CommonStatic.ctx.getWorkspaceFile("./.temp_" + id + "/musics/" + Data.trio(val) + ".ogg");
                Context.renameTo(f, fx);
                data.musics.set(val, new Music(new Identifier<>(id, Music.class, val), new FDFile(fx)));
            }
        }

        private void loadUnits(ISStream is) throws VerFixerException {
            int ver = Data.getVer(is.nextString());
            if (ver != 401)
                throw new VerFixerException("expect unit store version to be 401, got " + ver);
            UserProfile.setStatic(ID_FIXER, new IdFixer(Unit.class));
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int ind = is.nextInt();
                UnitLevel ul = new UnitLevel(new Identifier<>(id, UnitLevel.class, ind), is);
                data.unitLevels.set(ind, ul);
            }
            n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int ind = is.nextInt();
                Unit u = new Unit(new Identifier<>(id, Unit.class, ind));
                u.lv = Identifier.parseInt(is.nextInt(), UnitLevel.class).get();
                u.lv.units.add(u);
                u.max = is.nextInt();
                u.maxp = is.nextInt();
                u.rarity = is.nextInt();
                int m = is.nextInt();
                u.forms = new Form[m];
                for (int j = 0; j < m; j++) {
                    String name = is.nextString();
                    AnimCE ac = decodeAnim(".temp_" + id, is.subStream(), r);
                    CustomUnit cu = new CustomUnit();
                    cu.convertOldData(Data.getVer(is.nextString()), is);
                    u.forms[j] = new Form(u, j, name, ac, cu);
                }
                data.units.set(ind, u);
            }
            UserProfile.setStatic(ID_FIXER, null);
        }

        private void writeImgs(VImg img, String type, String name) throws IOException {
            String path = "./.temp_" + id + "/" + type + "/" + name;
            File f = CommonStatic.ctx.getWorkspaceFile(path);
            if(!f.exists())
                Context.check(f);
            String format = name.endsWith(".png") ? "PNG" : "";
            FakeImage.write(img.getImg(), format, f);
            img.unload();
        }
    }

    public static void fix(Map<String, VerFixer> map) throws Exception {
        transform();
        boolean clear = readPacks(map);

        //Close all ISStream before deleting
        for(VerFixer fix : map.values()) {
            if(fix.is != null)
                fix.is.close();
        }
        if (clear)
            Context.delete(CommonStatic.ctx.getAuxFile("./pack"));
    }

    private static VerFixer fix_bcupack(ISStream is, ImgReader r) throws Exception {
        int ver = Data.getVer(is.nextString());
        if (ver != 402)
            throw new VerFixerException("unexpected bcupack data version: " + ver + ", requires 402");
        InStream head = is.subStream();
        PackDesc desc = new PackDesc(Data.hex(head.nextInt()));
        int n = head.nextByte();
        for (int i = 0; i < n; i++)
            desc.dependency.add(Data.hex(head.nextInt()));
        desc.BCU_VERSION = Data.revVer(head.nextInt());
        if (!desc.BCU_VERSION.startsWith("4-11"))
            System.out.println("unexpected pack BCU version: " + desc.BCU_VERSION + ", requires 4.11.x");//throw new VerFixerException("unexpected pack BCU version: " + desc.BCU_VERSION + ", requires 4.11.x");
        desc.exportDate = head.nextString();
        desc.version = head.nextInt();
        desc.author = head.nextString();
        PackFixer fix = new PackFixer(desc.id, r);
        fix.data = new UserPack(desc, fix);
        fix.is = is;
        return fix;
    }

    private static void move(String a, String b) {
        File f = new File(a);
        if (!f.exists())
            return;
        File bf = new File(b);
        if (!bf.getParentFile().exists())
            bf.getParentFile().mkdirs();
        f.renameTo(new File(b));
    }

    private static boolean readPacks(Map<String, VerFixer> map) throws Exception {
        boolean alldone = true;
        File f = CommonStatic.ctx.getAuxFile("./pack/");
        Set<String> packs = new HashSet<>();
        if (f.exists() && f.isDirectory())
            for (File file : f.listFiles()) {

                String str = file.getName();
                if (!str.endsWith(".bcupack"))
                    continue;
                try {
                    VerFixer pack = fix_bcupack(new ISStream(file), getReader(file));//Dunno
                    map.put(pack.id, pack);
                    packs.add(pack.id);
                } catch (Exception e) {
                    alldone = false;
                    System.out.println("Failed to convert pack: " + str);
                    e.printStackTrace();
                }
            }
        List<VerFixer> list = new ArrayList<>(map.values());
        while (!list.isEmpty()) {
            int rem = 0;
            for (VerFixer p : list) {
                String loadProg;

                if(p.data != null) {
                    loadProg = "Fixing "+p.data.getSID()+".bcupack";
                } else {
                    loadProg = null;
                }
                CommonStatic.ctx.loadProg(rem * 1.0 / list.size(), loadProg);

                boolean all = true;
                for (String pre : p.data.desc.dependency)
                    if (!pre.equals(Identifier.DEF) && (!map.containsKey(pre) || map.get(pre).is != null))
                        all = false;
                if (all) {
                    p.data.desc.dependency.remove(Identifier.DEF);
                    p.load();
                    Workspace w = new Workspace(".temp_" + p.id);
                    p.data.source = w;
                    w.save(p.data, false);
                    w.export(p.data, p.data.desc.author + "." + p.id, "", false, (d) -> {});
                    File src = CommonStatic.ctx.getWorkspaceFile("./.temp_" + p.id);
                    Context.delete(src);
                    rem++;
                } else {
                    System.out.println("Couldn't fix pack " + p.data.desc.names + " due to missing parents: " + p.data.desc.dependency);
                }
            }
            list.removeIf(p -> p.is == null);
            if (rem == 0) {
                for (VerFixer p : list)
                    CommonStatic.ctx.printErr(ErrType.WARN, "Failed to load " + p.data.desc);
                break;
            }
        }
        return list.isEmpty() && alldone;
    }

    private static void transform() {
        File f = CommonStatic.ctx.getAuxFile("./res/anim/");
        if (!f.exists())
            return;
        for (String fi : f.list()) {
            String pa = "./res/anim/" + fi + "/";
            String pb = "./workspace/_local/animations/" + fi + "/";
            move(pa + fi + ".png", pb + SP);
            move(pa + "edi.png", pb + EDI);
            move(pa + "uni.png", pb + UNI);
            move(pa + fi + ".imgcut", pb + IC);
            move(pa + fi + ".mamodel", pb + MM);
            move(pa + fi + "00.maanim", pb + "maanim_walk.txt");
            move(pa + fi + "01.maanim", pb + "maanim_idle.txt");
            move(pa + fi + "02.maanim", pb + "maanim_attack.txt");
            move(pa + fi + "03.maanim", pb + "maanim_kb.txt");
            move(pa + fi + "_zombie00.maanim", pb + "maanim_burrow_down.txt");
            move(pa + fi + "_zombie01.maanim", pb + "maanim_burrow_move.txt");
            move(pa + fi + "_zombie02.maanim", pb + "maanim_burrow_up.txt");
        }
    }

    public UserPack data;

    ISStream is;

    private VerFixer(String id) {
        super(id);
    }

    @Override
    public void delete() {
    }

    @Override
    public FileData getFileData(String str) {
        return null;
    }

    @Override
    public String[] listFile(String str) {
        return null;
    }

    @Override
    public VImg readImage(String path, int ind) {
        return null;
    }

    @Override
    public HashSet<AnimCI> getAnims(BasePath path) {
        return null;
    }

    @Override
    public AnimCI loadAnimation(String name, BasePath base) {
        return null;
    }

    @Override
    public VImg readImage(String path) {
        return null;
    }

    @Deprecated
    protected AnimCE decodeAnim(String target, ISStream is, ImgReader r) {
        Source.SourceAnimLoader al = r == null ? new PCAL(target, is) : new PCAL(target, is, r);
        ResourceLocation id = al.getName();
        id.pack = target;
        Workspace.validate(id);
        AnimCE ce = new AnimCE(al);
        ce.check();
        new SourceAnimSaver(id, ce).saveAll();
        return new AnimCE(id);
    }

    protected abstract void load() throws Exception;

    public static Data.Proc fixProc(int[][] data) {
        Data.Proc ans = new Data.Proc();
        try {
            ans.KB.prob = data[Data.P_KB][0];
            ans.KB.time = data[Data.P_KB][0];
            ans.KB.dis = data[Data.P_KB][0];

            ans.STOP.prob = data[Data.P_STOP][0];
            ans.STOP.time = data[Data.P_STOP][1];

            ans.SLOW.prob = data[Data.P_SLOW][0];
            ans.SLOW.time = data[Data.P_SLOW][1];

            ans.CRIT.prob = data[Data.P_CRIT][0];
            ans.CRIT.mult = data[Data.P_CRIT][1];

            ans.WAVE.prob = data[Data.P_WAVE][0];
            ans.WAVE.lv = data[Data.P_WAVE][1];

            ans.WEAK.prob = data[5][0];
            ans.WEAK.time = data[5][1];
            ans.WEAK.mult = data[5][2];

            ans.BREAK.prob = data[6][0];

            ans.WARP.prob = data[7][0];
            ans.WARP.time = data[7][1];
            ans.WARP.dis = ans.WARP.dis_1 = data[7][2];

            ans.CURSE.prob = data[8][0];
            ans.CURSE.time = data[8][1];

            ans.STRONG.health = data[9][0];
            ans.STRONG.mult = data[9][1];

            ans.LETHAL.prob = data[10][0];

            ans.BURROW.count = data[11][0];
            ans.BURROW.dis = data[11][0];

            ans.REVIVE.count = data[12][0];
            ans.REVIVE.time = data[12][1];
            ans.REVIVE.health = data[12][2];
            ans.REVIVE.dis_0 = data[12][3];
            ans.REVIVE.dis_1 = data[12][4];
            int intType = data[12][5];
            ans.REVIVE.range_type = REVIVE.RANGE.values()[intType & (1 << 2) - 1];//intType >> i & (1 << BitCount.value) - 1
            ans.REVIVE.imu_zkill = (intType >> 3 & 1) == 1;
            ans.REVIVE.revive_non_zombie = (intType >> 4 & 1) == 1;
            ans.REVIVE.revive_others = (intType >> 5 & 1) == 1;

            ans.IMUKB.mult = data[13][0];
            ans.IMUSTOP.mult = data[14][0];
            ans.IMUSLOW.mult = data[15][0];
            ans.IMUWAVE.mult = data[16][0];
            ans.IMUWEAK.mult = data[17][0];
            ans.IMUWARP.mult = data[18][0];
            ans.IMUCURSE.mult = data[19][0];
            ans.SNIPER.prob = data[20][0];

            ans.TIME.prob = data[21][0];
            ans.TIME.time = data[21][1];

            ans.SEAL.prob = data[22][0];
            ans.SEAL.time = data[22][1];

            ans.SUMMON.prob = data[23][0];
            ans.SUMMON.id = Identifier.parseIntRaw(data[23][1], ans.SUMMON.getClass());
            ans.SUMMON.dis = ans.SUMMON.max_dis = data[23][2];
            ans.SUMMON.mult = data[23][3];
            intType = data[23][4];
            ans.SUMMON.anim_type = Data.Proc.SUMMON_ANIM.values()[intType & (1 << 2) - 1];
            ans.SUMMON.ignore_limit = (intType >> 3 & 1) == 1;
            ans.SUMMON.fix_buff = (intType >> 4 & 1) == 1;
            ans.SUMMON.same_health = (intType >> 5 & 1) == 1;
            ans.SUMMON.min_layer = (intType >> 6 & 1) == 1 ? 0 : -1;
            ans.SUMMON.max_layer = ans.SUMMON.min_layer == 0 ? 9 : -1;
            ans.SUMMON.on_hit = (intType >> 7 & 1) == 1;
            ans.SUMMON.on_kill = (intType >> 8 & 1) == 1;
            ans.SUMMON.time = data[23][5];

            ans.MOVEWAVE.prob = data[24][0];
            ans.MOVEWAVE.speed = data[24][1];
            ans.MOVEWAVE.width = data[24][2];
            ans.MOVEWAVE.time = data[24][3];
            ans.MOVEWAVE.dis = data[24][4];
            ans.MOVEWAVE.itv = data[24][5];

            ans.THEME.prob = data[25][0];
            ans.THEME.time = data[25][1];
            ans.THEME.id = Identifier.parseIntRaw(data[25][2], Background.class);
            intType = data[25][3];
            ans.THEME.kill = (intType & 1) == 1;

            ans.POISON.prob = data[26][0];
            ans.POISON.time = data[26][1];
            ans.POISON.damage = data[26][2];
            ans.POISON.itv = data[26][3];
            intType = data[26][4];
            ans.POISON.damage_type = POISON.TYPE.values()[intType & (1 << 2) - 1];
            ans.POISON.unstackable = (intType >> 3 & 1) == 1;

            ans.BOSS.prob = data[27][0];

            if (data[28][0] == 1)
                ans.CRITI.mult = 100;
            else if (data[28][0] == 2)
                ans.CRITI.block = 100;

            ans.SATK.prob = data[29][0];
            ans.SATK.mult = data[29][1];

            ans.IMUATK.prob = data[30][0];
            ans.IMUATK.time = data[30][1];

            ans.SATK.prob = data[31][0];
            ans.SATK.mult = data[31][1];

            ans.VOLC.prob = data[32][0];
            ans.VOLC.dis_0 = data[32][1];
            ans.VOLC.dis_1 = data[32][2];
            ans.VOLC.time = data[32][3];

            ans.IMUPOIATK.mult = data[33][0];
            ans.IMUVOLC.mult = data[34][0];

            ans.WEAK.prob = data[35][0];
            ans.WEAK.time = data[35][1];
            ans.WEAK.mult = data[35][2];

            ans.SPEED.prob = data[36][0];
            ans.SPEED.time = data[36][1];
            ans.SPEED.speed = data[36][2];
            ans.SPEED.type = SPEED.TYPE.values()[data[36][3]];
            //ans.MINIWAVE.prob = data[37][0];
            //ans.MINIWAVE.lv = data[37][1];
            //ans.MINIWAVE.multi = data[37][2];
        } catch (Exception ignored) {
        }
        return ans;
    }

    public static ImgReader getReader(File f) {
        return null;
    }
}
