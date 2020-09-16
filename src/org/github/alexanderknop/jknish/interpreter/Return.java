package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.KnishObject;

class Return extends RuntimeException {
    final KnishObject value;

    Return(KnishObject value) {
        super(null, null, false, false);
        this.value = value;
    }
}
