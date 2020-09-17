package org.github.alexanderknop.jknish.resolver;

import java.util.Map;
import java.util.Objects;

public class ResolvedScript {
    public final ResolvedStatement.Block code;
    public final Map<Integer, String> globals;

    public ResolvedScript(ResolvedStatement.Block code, Map<Integer, String> globals) {
        this.code = code;
        this.globals = globals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolvedScript that = (ResolvedScript) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(globals, that.globals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, globals);
    }

    @Override
    public String toString() {
        return "ResolvedScript{" +
                "code=" + code +
                ", globals=" + globals +
                '}';
    }
}
