package link.infra.jumploader.launch;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Function;

public class ReflectionUtil {
	private static final Unsafe UNSAFE;
	private static final MethodHandles.Lookup IMPL_LOOKUP;
	static {
		try {
			final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
			final Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			IMPL_LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(implLookupField), UNSAFE.staticFieldOffset(implLookupField));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T reflectField(Object destObj, String name) throws NoSuchFieldException, IllegalAccessException, ClassCastException {
		try {
			Field field = destObj.getClass().getDeclaredField(name);
			return (T) IMPL_LOOKUP.unreflectGetter(field).invoke(destObj);
		} catch (RuntimeException | Error | NoSuchFieldException | IllegalAccessException e) {
			throw e;
		} catch (Throwable e) {
			return ThrowableUtil.sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T reflectStaticField(Class<?> destClass, String name) throws NoSuchFieldException, IllegalAccessException, ClassCastException {
		try {
			Field field = destClass.getDeclaredField(name);
			return (T) IMPL_LOOKUP.unreflectGetter(field).invoke();
		} catch (RuntimeException | Error | NoSuchFieldException | IllegalAccessException e) {
			throw e;
		} catch (Throwable e) {
			return ThrowableUtil.sneakyThrow(e);
		}
	}

	@SuppressWarnings({"unchecked", "UnusedReturnValue"})
	public static <T> T transformStaticField(Class<?> destClass, String name, Function<T, T> transformer) throws NoSuchFieldException, IllegalAccessException, ClassCastException {
		try {
			Field field = destClass.getDeclaredField(name);
			T value = transformer.apply((T) IMPL_LOOKUP.unreflectGetter(field).invoke());
			IMPL_LOOKUP.unreflectSetter(field).invoke(value);
			return value;
		} catch (RuntimeException | Error | NoSuchFieldException | IllegalAccessException e) {
			throw e;
		} catch (Throwable e) {
			return ThrowableUtil.sneakyThrow(e);
		}
	}
}
