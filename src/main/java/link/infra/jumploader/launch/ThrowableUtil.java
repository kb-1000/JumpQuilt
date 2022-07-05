package link.infra.jumploader.launch;

@SuppressWarnings("unchecked")
public class ThrowableUtil {
    public static <R, T extends Throwable> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
