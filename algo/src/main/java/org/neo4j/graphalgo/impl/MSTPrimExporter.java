package org.neo4j.graphalgo.impl;

import org.neo4j.graphalgo.core.utils.Exporter;
import org.neo4j.graphalgo.core.utils.container.UndirectedTree;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

/**
 * @author mknblch
 */
public class MSTPrimExporter extends Exporter<UndirectedTree> {



    public MSTPrimExporter(GraphDatabaseAPI api) {
        super(api);
    }

    @Override
    public void write(UndirectedTree data) {

    }
}
