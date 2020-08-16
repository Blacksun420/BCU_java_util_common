package common.io.json;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface JsonClass {

	/** indicates that this constructor is only used by JSON */
	@Target(ElementType.CONSTRUCTOR)
	public static @interface JCConstructor {
	}

	/** indicates that this class can be loaded with a value of another class */
	@Documented
	@Retention(RUNTIME)
	@Target(TYPE)
	public static @interface JCGeneric {
		Class<?>[] value();
	}

	@Documented
	@Retention(RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface JCGetter {
	}

	@Documented
	@Retention(RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface JCIdentifier {
	}

	public static enum NoTag {
		OMIT, LOAD
	}

	public static enum RType {
		/**
		 * generated from json, requires default constructor, no not allow generate tag
		 */
		DATA,
		/** generated by holder class, requires generator tag */
		FILL,
		/** generated from json, requires generator method with parameter JsonObject */
		MANUAL
	}

	public static enum WType {
		DEF, CLASS
	}

	/** treat this class as collection */
	boolean bypass() default false;

	String generator() default "";

	/** determines how to reat fields with no JsonFiel annotation */
	JsonClass.NoTag noTag() default NoTag.OMIT;

	JsonClass.RType read() default RType.DATA;

	String serializer() default "";

	JsonClass.WType write() default WType.DEF;

}