package org.github.alexanderknop.jknish;

import java.util.List;

/**
 * An interface for all the objects that the Knish interpreter uses.
 */
public interface KnishObject {
    /**
     * @param arguments the list of arguments of the method call; note that
     *                  it can be null, in this case we interpret it as the
     *                  call of a getter.
     * @return the result of the method call.
     */
    KnishObject call(int line, String method, List<KnishObject> arguments);
}
