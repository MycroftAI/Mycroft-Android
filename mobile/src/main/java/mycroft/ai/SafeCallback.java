package mycroft.ai;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

/**
 * Inversion of the {@link java.util.concurrent.Callable} interface.
 * <p>
 *     Note that the {@link #call(Object)} method in this class is
 *     not allowed to throw exceptions.
 * </p>
 *
 * @author Philip Cohn-Cort
 */
public interface SafeCallback<T> {
	/**
	 * Variant of {@link Callable#call()}
	 * @param param any value. May be null.
	 */
	void call(@NonNull T param);
}
