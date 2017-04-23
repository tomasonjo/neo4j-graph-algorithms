package org.neo4j.graphalgo.results;

/**
 * @author mknblch
 */
public class DijkstraResult {

    public final Long loadDuration;
    public final Long evalDuration;
    public final Long writeDuration;

    public DijkstraResult(Long loadDuration, Long evalDuration, Long writeDuration) {
        this.loadDuration = loadDuration;
        this.evalDuration = evalDuration;
        this.writeDuration = writeDuration;
    }
}
