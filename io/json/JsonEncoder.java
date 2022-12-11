package common.io.json;

import com.google.gson.*;
import common.io.json.JsonClass.JCGeneric;
import common.io.json.JsonClass.JCIdentifier;
import common.io.json.JsonException.Type;
import common.util.Data;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JsonEncoder {

	public static JsonElement encode(Object obj) {
		return Data.err(() -> encode(obj, null));
	}

	private static JsonElement encode(Object obj, JsonEncoder par) throws Exception {
		if (obj == null)
			return JsonNull.INSTANCE;
		if (obj instanceof JsonElement)
			return (JsonElement) obj;
		if (obj instanceof Number)
			return new JsonPrimitive((Number) obj);
		if (obj instanceof Boolean)
			return new JsonPrimitive((Boolean) obj);
		if (obj instanceof String)
			return new JsonPrimitive((String) obj);
		if (obj instanceof Enum)
			return encode(obj.toString(), par);
		if (obj instanceof Class)
			return new JsonPrimitive(((Class<?>) obj).getName());
		Class<?> cls = obj.getClass();
		if (cls.isArray()) {
			if (par != null && par.curjfld != null && par.curjfld.usePool()) {
				JsonField.Handler handler = new JsonField.Handler();
				int n = Array.getLength(obj);
				JsonArray jarr = new JsonArray();
				for (int i = 0; i < n; i++)
					jarr.add(handler.add(Array.get(obj, i)));
				JsonObject jobj = new JsonObject();
				jobj.add("pool", encode(handler.list));
				jobj.add("data", jarr);
				return jobj;
			}
			int n = Array.getLength(obj);
			JsonArray arr = new JsonArray(n);
			for (int i = 0; i < n; i++)
				arr.add(encode(Array.get(obj, i), par));
			return arr;
		}
		if (cls.getAnnotation(JCGeneric.class) != null && par != null && par.curjfld.alias().length > par.index) {
			JCGeneric jcg = cls.getAnnotation(JCGeneric.class);
			Class<?> alias = par.curjfld.alias()[par.index];
			boolean found = false;
			for (Class<?> ala : jcg.value())
				if (ala == alias) {
					found = true;
					break;
				}
			if (!found)
				throw new JsonException(Type.TYPE_MISMATCH, null, cls + " not present in JCGeneric");
			for (Field f : cls.getFields()) {
				JCIdentifier jcgw = f.getAnnotation(JCIdentifier.class);
				if (jcgw != null && f.getType() == alias)
					return encode(f.get(obj), par);
			}
			Constructor<?> con = getFuncConstructor(alias, cls);
			return encode(con.newInstance(obj), par);
		}
		if (par != null && par.curjfld != null) {
			JsonField jfield = par.curjfld;
			if (jfield.ser() == JsonField.SerType.FUNC) {
				if (jfield.serializer().length() == 0)
					throw new JsonException(Type.FUNC, null, "no serializer function");
				Method m = par.obj.getClass().getMethod(jfield.serializer(), cls);
				return encode(m.invoke(par.obj, obj));
			} else if (jfield.ser() == JsonField.SerType.CLASS) {
				JsonClass cjc = cls.getAnnotation(JsonClass.class);
				if (cjc == null || cjc.serializer().length() == 0)
					throw new JsonException(Type.FUNC, null, "no serializer function");
				String func = cjc.serializer();
				Method m = cls.getMethod(func);
				return encode(m.invoke(obj), null);
			}
		}
		JsonClass jc = cls.getAnnotation(JsonClass.class);
		if (jc != null)
			if (jc.write() == JsonClass.WType.DEF)
				return new JsonEncoder(par, obj).ans;
			else if (jc.write() == JsonClass.WType.CLASS) {
				if (jc.serializer().length() == 0)
					throw new JsonException(Type.FUNC, null, "no serializer function");
				String func = jc.serializer();
				Method m = cls.getMethod(func);
				return encode(m.invoke(obj), null);
			}
		if (obj instanceof List)
			return encodeList((List<?>) obj, par);
		if (obj instanceof Set)
			return encodeSet((Set<?>) obj, par);
		if (obj instanceof Map)
			return encodeMap((Map<?, ?>) obj, par);
		throw new JsonException(Type.UNDEFINED, null, "object " + obj + ":" + obj.getClass() + " not defined");
	}

	/***
	 * Gets a valid constructor from a class, and in case no constructor is found with it, checks if there's a valid option using its interfaces or superclass
	 * @param alias The class intended to be constructed as
	 * @param cls The current class used as parameter for the constructor
	 * @return A valid constructor that takes the given class as parameter
	 * @throws JsonException If this class, and none of it's superclasses or interfaces has a JCGeneric constructor that uses the given class
	 */
	private static Constructor<?> getFuncConstructor(Class<?> alias, Class<?> cls) throws JsonException {
		try {
			return alias.getConstructor(cls);
		} catch (Exception e) {
			if (cls.getSuperclass() != null && cls.getSuperclass().getAnnotation(JCGeneric.class) != null) {
				JCGeneric jcg = cls.getSuperclass().getAnnotation(JCGeneric.class);
				for (Class<?> ala : jcg.value())
					if (ala == alias)
						return getFuncConstructor(alias, cls.getSuperclass());
			}
			for (Class<?> intf : cls.getInterfaces()) {
				if (intf.getAnnotation(JCGeneric.class) != null) {
					JCGeneric jcg = intf.getAnnotation(JCGeneric.class);
					for (Class<?> ala : jcg.value())
						if (ala == alias)
							return getFuncConstructor(alias, intf);
				}
			}
			throw new JsonException(Type.FUNC, null, "No constructor using " + alias + " found for " + cls);
		}
	}

	private static JsonElement encodeList(List<?> list, JsonEncoder par) throws Exception {
		if (par != null && par.curjfld != null && par.curjfld.usePool()) {
			JsonField.Handler handler = new JsonField.Handler();
			JsonArray jarr = new JsonArray(list.size());
			for (Object o : list) jarr.add(handler.add(o));

			JsonObject jobj = new JsonObject();
			jobj.add("pool", encode(handler.list));
			jobj.add("data", jarr);
			return jobj;
		}
		JsonArray ans = new JsonArray(list.size());
		for (Object obj : list)
			ans.add(encode(obj, par));
		return ans;
	}

	private static JsonArray encodeMap(Map<?, ?> map, JsonEncoder par) throws Exception {
		JsonArray ans = new JsonArray(map.size());
		for (Entry<?, ?> obj : map.entrySet()) {
			JsonObject ent = new JsonObject();
			ent.add("key", encode(obj.getKey(), par));
			par.index = 1;
			ent.add("val", encode(obj.getValue(), par));
			par.index = 0;
			ans.add(ent);
		}
		return ans;
	}

	private static JsonArray encodeSet(Set<?> set, JsonEncoder par) throws Exception {
		JsonArray ans = new JsonArray(set.size());
		for (Object obj : set)
			ans.add(encode(obj, par));
		return ans;
	}

	private final JsonEncoder par;
	private final Object obj;
	private final JsonObject ans = new JsonObject();

	private JsonClass curjcls;
	private JsonField curjfld;
	private int index = 0;

	private JsonEncoder(JsonEncoder parent, Object object) throws Exception {
		par = parent;
		obj = object;
		encode(obj.getClass());
	}

	private void encode(Class<?> cls) throws Exception {
		if (cls.getSuperclass().getAnnotation(JsonClass.class) != null)
			encode(cls.getSuperclass());
		curjcls = cls.getAnnotation(JsonClass.class);
		for (Field f : cls.getDeclaredFields())
			if (curjcls.noTag() == JsonClass.NoTag.LOAD || f.getAnnotation(JsonField.class) != null) {
				if (Modifier.isStatic(f.getModifiers()))
					continue;
				JsonField jf = f.getAnnotation(JsonField.class);
				if (jf == null)
					jf = JsonField.DEF;
				if (jf.block() || jf.io() == JsonField.IOType.R)
					continue;
				String tag = jf.tag().length() == 0 ? f.getName() : jf.tag();
				f.setAccessible(true);
				curjfld = jf;
				Object val = f.get(obj);
				JsonElement elem = encode(val, getInvoker());
				if (elem.isJsonObject() && curjfld.alias().length == 0 && val != null && val.getClass() != f.getType())
					elem.getAsJsonObject().addProperty("_class", val.getClass().getName());
				ans.add(tag, elem);
				curjfld = null;
			}
		for (Method m : cls.getDeclaredMethods())
			if (m.getAnnotation(JsonField.class) != null) {
				JsonField jf = m.getAnnotation(JsonField.class);
				if (jf.io() == JsonField.IOType.R)
					continue;
				if (jf.io() == JsonField.IOType.RW)
					throw new JsonException(Type.FUNC, null, "functional fields should not have RW type");
				String tag = jf.tag();
				if (tag.length() == 0)
					throw new JsonException(Type.TAG, null, "function fields must have tag");
				curjfld = jf;
				ans.add(tag, encode(m.invoke(obj), getInvoker()));
				curjfld = null;
			}
	}

	private JsonEncoder getInvoker() {
		return curjcls.bypass() ? par : this;
	}

}
