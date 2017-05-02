package org.neo4j.graphalgo.results;

import org.neo4j.graphalgo.core.utils.ProgressTimer;

/**
 * @author mknblch
 */
public class MSTPrimResult {

    public final Long loadDuration;
    public final Long evalDuration;
    public final Long writeDuration;
    public final Double weightSum;

    public MSTPrimResult(Long loadDuration, Long evalDuration, Long writeDuration, Double weightSum) {
        this.loadDuration = loadDuration;
        this.evalDuration = evalDuration;
        this.writeDuration = writeDuration;
        this.weightSum = weightSum;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected long loadDuration = -1;
        protected long evalDuration = -1;
        protected long writeDuration = -1;
        protected double weightSum = 0.0;

        public Builder withWeightSum(double weightSum) {
            this.weightSum = weightSum;
            return this;
        }

        public Builder withLoadDuration(long loadDuration) {
            this.loadDuration = loadDuration;
            return this;
        }

        public Builder withEvalDuration(long evalDuration) {
            this.evalDuration = evalDuration;
            return this;
        }

        public Builder withWriteDuration(long writeDuration) {
            this.writeDuration = writeDuration;
            return this;
        }

        public MSTPrimResult build() {
            return new MSTPrimResult(loadDuration, evalDuration, writeDuration, weightSum);
        }
    }
}
