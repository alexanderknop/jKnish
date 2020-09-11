package org.github.alexanderknop.jknish.objects;

import java.util.List;

public interface KnishMethod {
    KnishObject call(List<KnishObject> arguments);
}
