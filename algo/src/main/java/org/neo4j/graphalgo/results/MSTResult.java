package org.neo4j.graphalgo.results;

import org.neo4j.graphalgo.core.utils.ProgressTimer;

/**
 * @author mknblch
 */
public class MSTResult {

    public final Long loadDuration;
    public final Long evalDuration;

    public MSTResult(Long loadDuration, Long evalDuration) {
        this.loadDuration = loadDuration;
        this.evalDuration = evalDuration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected long loadDuration = -1;
        protected long evalDuration = -1;

        public ProgressTimer load() {
            return ProgressTimer.start(res -> loadDuration = res);
        }

        public ProgressTimer eval() {
            return ProgressTimer.start(res -> evalDuration = res);
        }

        public MSTResult build() {
            return null;
        }
    }
}
