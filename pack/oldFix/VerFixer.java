package common.pack.oldFix;

import com.google.common.io.Files;
import common.CommonStatic;
import common.battle.data.AtkDataModel;
import common.battle.data.CustomEnemy;
import common.battle.data.CustomEntity;
import common.battle.data.CustomUnit;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.Identifier;
import common.pack.PackData.PackDesc;
import common.pack.PackData.UserPack;
import common.pack.Source;
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
import common.util.pack.Soul;
import common.util.stage.CastleImg;
import common.util.stage.MapColc.PackMapColc;
import common.util.stage.Music;
import common.util.unit.*;
import common.util.unit.rand.EREnt;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static common.pack.Source.SourceAnimLoader.*;
import static common.util.unit.Character.reorderAbi;

@SuppressWarnings("deprecation")
public abstract class VerFixer extends Source {

    private static Class<?> idFix = null;

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
    public static ImgReader getReader(File f) {
        return null;
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
            String path = "./.temp_" + Data.hex(pid) + "/musics/" + Data.trio(mid) + ".ogg";
            File f = CommonStatic.ctx.getWorkspaceFile(path);
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

    private static class PackFixer extends VerFixer {

        private final ImgReader r;
        private final int ver;

        public PackFixer(String id, int ver, ImgReader r) {
            super(id);
            this.ver = ver;
            this.r = r;
        }

        @Override
        protected void load() throws Exception {
            data.desc.names.put(is.nextString());
            if (ver >= 401) {
                loadEnemies(is.subStream());
                loadUnits(is.subStream());
                loadCastles(is.subStream());
                loadBackgrounds(is.subStream());
                if (ver == 402)
                    loadMusics(is.subStream());
            } else if (ver >= 303) {
                loadEnemies$303(is, r);
                if (ver >= 306) {
                    loadCastles(is.subStream());
                    loadBackgrounds(is.subStream());
                }
            }
            data.mc = new PackMapColc(data, is);
            is.close();
            is = null;
        }

        private void loadBackgrounds(ISStream is) throws Exception {
            int version = ver >= 309 ? Data.getVer(is.nextString()) : ver;
            if (version < 400) {
                is.nextInt();
                return;
            }
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
            int version = ver >= 307 ? Data.getVer(is.nextString()) : ver;
            if (version < 306)
                return;//Useless as checked above, solely kept to extract the is.nextString() effect
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int val = is.nextInt();
                VImg vimg = ImgReader.readImg(is, r);
                vimg.name = Data.trio(val);
                writeImgs(vimg, "castles", vimg.name + ".png");
                data.castles.set(val, new CastleImg(new Identifier<>(id, CastleImg.class, val), vimg));
            }
        }

        private void loadEnemies(ISStream is) {
            idFix = AbEnemy.class;
            int ver = Data.getVer(is.nextString());
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int hash = is.nextInt();
                String str = is.nextString();
                CustomEnemy ce = new CustomEnemy();
                convertOldEnemyData(ce, ver, is);
                AnimCE ac = decodeAnim(".temp_" + id, is.subStream(), r);
                Enemy e = new Enemy(new Identifier<>(id, Enemy.class, hash % 1000), ac, ce);
                e.names.put(str);
                //ce.limit = CommonStatic.customEnemyMinPos(ac.loader.getMM());
                if (ce.tba != 0)
                    ce.tba += ce.getPost(false, 0) + 1;
                data.enemies.set(hash % 1000, e);
            }
            if (ver != 402)
                return;
            n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int hash = is.nextInt();
                EneRand e = new EneRand(new Identifier<>(id, EneRand.class, hash % 1000));
                convertOldRandomEnemyData(e, is.subStream());
                data.randEnemies.set(hash % 1000, e);
            }
        }

        private void loadEnemies$303(ISStream is, ImgReader r) {
            idFix = AbEnemy.class;
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int hash = is.nextInt();
                String str = is.nextString();
                CustomEnemy ce = new CustomEnemy();
                convertOldEnemyData(ce, ver, is);
                AnimCE ac = decodeAnim(".temp_" + id, is.subStream(), r);
                Enemy e = new Enemy(new Identifier<>(id, Enemy.class, hash % 1000), ac, ce);
                e.names.put(str);
                data.enemies.set(hash % 1000, e);
            }
        }

        private void loadMusics(ISStream is) {
            int ver = Data.getVer(is.nextString());
            if (ver != 307)
                return;
            int n = is.nextInt();
            for (int i = 0; i < n; i++) {
                int val = is.nextInt();
                File f = ImgReader.loadMusicFile(is, r, Integer.parseInt(id), val);
                //File fx = CommonStatic.ctx.getWorkspaceFile("./.temp_" + id + "/musics/" + Data.trio(val) + ".ogg");
                //Context.renameTo(f, fx);
                data.musics.set(val, new Music(new Identifier<>(id, Music.class, val), new FDFile(f)));
            }
        }

        private void loadUnits(ISStream is) {
            int ver = Data.getVer(is.nextString());
            if (ver < 401)
                return;
            idFix = Unit.class;
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
                    convertOldUnitData(cu, Data.getVer(is.nextString()), is);
                    u.forms[j] = new Form(u, j, name, ac, cu);
                    if (cu.tba != 0)
                        cu.tba += cu.getPost(false, 0) + 1;
                    //cu.limit = CommonStatic.customFormMinPos(ac.loader.getMM());
                }
                data.units.set(ind, u);
            }
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
        for(VerFixer fix : map.values())
            if(fix.is != null)
                fix.is.close();
        if (clear)
            Context.delete(CommonStatic.ctx.getAuxFile("./pack"));
    }

    private static VerFixer fix_bcupack(ISStream is, ImgReader r) {
        int ver = Data.getVer(is.nextString());
        if (ver >= 400) {
            ISStream head = is.subStream();
            PackDesc desc = new PackDesc(Data.hex(head.nextInt()));
            if (ver != 402)
                System.out.println(desc.id + " ver is " + ver);
            int n = head.nextByte();
            for (int i = 0; i < n; i++)
                desc.dependency.add(Data.hex(head.nextInt()));

            int bcuver = head.nextInt();
            // mistake handling
            if (bcuver == 406010)
                bcuver = 40610;
            desc.BCU_VERSION = Data.getVer(bcuver);
            if (!desc.BCU_VERSION.startsWith("4.11"))
                System.out.println("unexpected pack BCU version: " + desc.BCU_VERSION + ", requires 4.11.x");//throw new VerFixerException("unexpected pack BCU version: " + desc.BCU_VERSION + ", requires 4.11.x");
            desc.FORK_VERSION = 0;
            String time = head.nextString();
            if (ver >= 402)
                desc.exportDate = time;
            else if (time.length() == 14)
                desc.exportDate = time.substring(4, 6) + " " + time.substring(6, 8) + " " + time.substring(0, 4) + " " + time.substring(8, 10) + ":" + time.substring(10, 12) + ":" + time.substring(12);
            desc.version = head.nextInt();
            if (ver >= 401)
                desc.author = head.nextString();
            PackFixer fix = new PackFixer(desc.id, ver, r);
            fix.data = new UserPack(desc, fix);
            fix.is = is;
            return fix;
        }
        PackDesc desc = new PackDesc();
        desc.id = Data.hex(is.nextInt());
        desc.BCU_VERSION = Data.getVer(ver * 100);
        desc.FORK_VERSION = 0;
        System.out.println(desc.id + " ver is old " + ver + "(" + desc.BCU_VERSION + ")");
        int n = is.nextByte();
        for (int i = 0; i < n; i++)
            desc.dependency.add(Data.hex(is.nextInt()));
        PackFixer fix = new PackFixer(desc.id, ver, r);
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

    //----------------------------------------------------------------------------------|-ENTITY DATA-|----------------------------------------------------------------------------------//
    private static void convertOldEntityDef(CustomEntity ce, int ver, ISStream is) {
        ce.hp = is.nextInt();
        ce.hb = is.nextInt();
        ce.speed = ver >= 308 ? is.nextInt() : is.nextByte();
        ce.range = ver >= 308 ? is.nextInt() : is.nextShort();
        ce.abi = is.nextInt();
        if ((ce.abi & 32768) > 0)
            ce.loop = 1;
        int type = is.nextInt();
        ce.traits = Trait.convertBitmask(Trait.reorderTrait(type), false);
        ce.width = ver >= 308 ? is.nextInt() : is.nextShort();
        ce.getProc().BARRIER.health = is.nextInt();
    }

    private static void convertOldEntityData(CustomEntity ce, int ver, ISStream is) {
        if (ver >= 400)
            ver = Data.getVer(is.nextString());
        convertOldEntityDef(ce,ver,is);
        if (ver >= 400) {
            ce.tba = is.nextInt();
            ce.base = is.nextInt();
            ce.touch = is.nextInt();
            boolean isrange = false;
            if (ver >= 403) {
                ce.loop = is.nextInt();
                if (ver >= 404)
                    ce.death = Identifier.parseInt(is.nextInt(), Soul.class);
            } else
                isrange = is.nextInt() > 0;
            ce.common = is.nextInt() > 0;
            ce.rep = new AtkDataModel(ce, is, ver);
            int m = is.nextInt();
            AtkDataModel[] set = new AtkDataModel[m];
            for (int i = 0; i < m; i++) {
                set[i] = new AtkDataModel(ce, is, ver);
                if (ver == 400)
                    set[i].range = isrange;
            }

            int n = is.nextInt();
            AtkDataModel[] atks = new AtkDataModel[n];
            for (int i = 0; i < n; i++)
                atks[i] = set[is.nextInt()];
            ce.hits.clear();
            ce.hits.add(atks);
            if (ver >= 401) {
                int adi = is.nextInt();
                if ((adi & 1) > 0)
                    ce.revs = new AtkDataModel[]{new AtkDataModel(ce, is, ver)};
                if ((adi & 2) > 0)
                    ce.ress = new AtkDataModel[]{new AtkDataModel(ce, is, ver)};
            }
        } else {//Proven before that ver >= 308 here
            boolean isrange = is.nextByte() > 0;
            ce.tba = is.nextInt();
            ce.base = is.nextInt();
            ce.common = is.nextByte() > 0;
            ce.rep = new AtkDataModel(ce, is, ver);
            int m = is.nextInt();
            AtkDataModel[] set = new AtkDataModel[m];
            for (int i = 0; i < m; i++) {
                set[i] = new AtkDataModel(ce, is, ver);
                set[i].range = isrange;
            }
            int n = is.nextInt();
            AtkDataModel[] atks = new AtkDataModel[n];
            for (int i = 0; i < n; i++)
                atks[i] = set[is.nextInt()];
            ce.hits.clear();
            ce.hits.add(atks);
        }
    }

    private static void injectOld(CustomEntity ce) {
        if ((ce.abi & (1 << 18)) != 0) //Seal Immunity
            ce.getProc().IMUSEAL.mult = 100;
        if ((ce.abi & (1 << 7)) != 0) //Moving atk Immunity
            ce.getProc().IMUMOVING.mult = 100;
        if ((ce.abi & (1 << 12)) != 0) //Poison Immunity
            ce.getProc().IMUPOI.mult = 100;
        ce.abi = reorderAbi(ce.abi, 0);

        boolean bounty = (ce.abi & 16) > 0;
        boolean atkbase = (ce.abi & 32) > 0;
        for (AtkDataModel atk : ce.getAllAtkModels()) {
            if (atk.getProc().POISON.prob > 0)
                atk.getProc().POISON.ignoreMetal = true;
            if (atk.getProc().SUMMON.prob > 0)
                if (atk.getProc().SUMMON.id != null && !AbEnemy.class.isAssignableFrom(atk.getProc().SUMMON.id.cls))
                    atk.getProc().SUMMON.fix_buff = true;

            if (bounty) //2x money
                atk.getProc().BOUNTY.mult = 100;
            if (atkbase) //base destroyer
                atk.getProc().ATKBASE.mult = 300;
        }
        ce.abi = reorderAbi(ce.abi, 1);

        if ((ce.abi & 32) > 0)
            ce.getProc().IMUWAVE.block = 100;
        ce.abi = reorderAbi(ce.abi, 2);

        ce.getProc().DMGINC.mult = 100;
        ce.getProc().DEFINC.mult = 100;
        if ((ce.abi & 1) != 0) {
            ce.getProc().DMGINC.mult *= 1.5;
            ce.getProc().DEFINC.mult *= 2;
        }
        if ((ce.abi & 2) != 0)//res
            ce.getProc().DEFINC.mult *= 4;
        if ((ce.abi & 4) != 0)//mas dmg
            ce.getProc().DMGINC.mult *= 3;
        if ((ce.abi & 16384) != 0)//ins res
            ce.getProc().DEFINC.mult *= 6;
        if ((ce.abi & 32768) != 0)//ins dmg
            ce.getProc().DMGINC.mult *= 5;

        ce.abi = reorderAbi(ce.abi, 3);
        if (ce.getProc().DMGINC.mult == 100)
            ce.getProc().DMGINC.mult = 0;
        if (ce.getProc().DEFINC.mult == 100)
            ce.getProc().DEFINC.mult = 0;
    }

    private static void removeOldEAbi(CustomEntity ce) {
        if ((ce.abi & 128) > 0)
            ce.getProc().IMUWAVE.mult = 100;
        if ((ce.abi & 512) > 0)
            ce.getProc().IMUKB.mult = 100;
        if ((ce.abi & 1024) > 0)
            ce.getProc().IMUSTOP.mult = 100;
        if ((ce.abi & 2048) > 0)
            ce.getProc().IMUSLOW.mult = 100;
        if ((ce.abi & 4096) > 0)
            ce.getProc().IMUWEAK.mult = 100;
        if ((ce.abi & 65536) > 0)
            ce.getProc().IMUWARP.mult = 100;
        if ((ce.abi & 262144) > 0)
            ce.getProc().IMUCURSE.mult = 100;
        ce.abi &= 0x7ae17f;
    }
    //----------------------------------------------------------------------------------|-ENEMY DATA-|----------------------------------------------------------------------------------//
    private static void convertOldEnemyData(CustomEnemy ce, int ver, ISStream is) {
        if (ver >= 307)
            ver = Data.getVer(is.nextString());
        if (ver >= 308) {
            convertOldEntityData(ce, ver, is);
            ce.star = is.nextByte();
            ce.drop = is.nextInt();
        } else if (ver >= 301)
            convertAncientEnemy(ce, is, ver);
        //injectOld(ce);
    }
    //I was born in 200-
    private static void convertAncientEnemy(CustomEnemy ce, ISStream is, int ver) {
        convertOldEntityDef(ce,ver,is);
        boolean isrange = is.nextByte() == 1;
        ce.tba = is.nextInt();
        ce.base = is.nextShort();
        ce.star = is.nextByte();
        ce.drop = is.nextInt();
        ce.common = ver >= 305 && is.nextByte() == 1;
        ce.rep = new AtkDataModel(ce, is, ver);
        int m = is.nextByte();
        AtkDataModel[] set = new AtkDataModel[m];
        for (int i = 0; i < m; i++) {
            set[i] = new AtkDataModel(ce, is, ver);
            set[i].range = isrange;
        }
        int n = is.nextByte();
        AtkDataModel[] atks = new AtkDataModel[n];
        for (int i = 0; i < n; i++)
            atks[i] = set[is.nextByte()];
        ce.hits.clear();
        ce.hits.add(atks);

        // eliminate old stuff
        if (ver < 307)
            removeOldEAbi(ce);
    }
    //----------------------------------------------------------------------------------|-RANDOM ENEMY DATA-|----------------------------------------------------------------------------------//
    public void convertOldRandomEnemyData(EneRand er, ISStream is) {
        int ver = Data.getVer(is.nextString());
        if (ver < 400)
            return;
        er.name = is.nextString();
        er.type = is.nextInt();
        int n = is.nextInt();
        for (int i = 0; i < n; i++) {
            EREnt ere = new EREnt();
            er.list.add(ere);
            ere.ent = Identifier.parseInt(is.nextInt(), AbEnemy.class);
            ere.multi = is.nextInt();
            ere.share = is.nextInt();
        }
    }
    //----------------------------------------------------------------------------------|-UNIT DATA-|----------------------------------------------------------------------------------//
    public void convertOldUnitData(CustomUnit cu, int ver, ISStream is) {
        if (ver >= 400) {
            convertOldEntityData(cu, ver, is);
            cu.price = is.nextInt();
            cu.resp = is.nextInt();
        }
        //injectOld(cu);
    }
    //----------------------------------------------------------------------------------|-PROC DATA-|----------------------------------------------------------------------------------//
    public static Data.Proc fixProc(int[][] data) {
        Data.Proc ans = new Data.Proc();
        try {
            ans.KB.prob = data[0][0];
            ans.KB.time = data[0][0];
            ans.KB.dis = data[0][0];

            ans.STOP.prob = data[1][0];
            ans.STOP.time = data[1][1];

            ans.SLOW.prob = data[2][0];
            ans.SLOW.time = data[2][1];

            ans.CRIT.prob = data[3][0];
            ans.CRIT.mult = data[3][1];

            ans.WAVE.prob = data[4][0];
            ans.WAVE.lv = data[4][1];

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
            ans.SUMMON.id = Identifier.parseIntRaw(data[23][1], idFix);
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
            ans.THEME.id = Identifier.parseInt(data[25][2], Background.class);
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
}
