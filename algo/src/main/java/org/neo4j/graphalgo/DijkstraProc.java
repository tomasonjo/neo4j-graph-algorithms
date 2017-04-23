package org.neo4j.graphalgo;

import algo.Pools;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.heavyweight.HeavyGraphFactory;
import org.neo4j.graphalgo.impl.ShortestPathDijkstra;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author mknblch
 */
public class DijkstraProc {

    public static final String CONFIG_LABEL = "label";
    public static final String CONFIG_RELATIONSHIP = "relationship";
    public static final String CONFIG_PROPERTY = "property";
    public static final String CONFIG_DEFAULT_VALUE = "defaultValue";

    @Context
    public GraphDatabaseAPI api;

    @Context
    public Log log;

    @Procedure("algo.dijkstra")
    @Description("CALL algo.dijkstra(startNodeId:long, endNodeId:long, " +
            "{label:'labelName*', relationship:'relationshipName*', property:'propertyName*', defaultValue:1.0}) " +
            "YIELD nodeId - yields a stream of nodeId from start to end (inclusive)")
    public Stream<ShortestPathNode> dijkstraShortestPath(
            @Name("startNode") Node startNodeId,
            @Name("endNode") Node endNodeId,
            @Name(value = "config", defaultValue = "{}")
                    Map<String, Object> config) {

        final Graph graph = new GraphLoader(api)
                .withOptionalLabel((String) config.get(CONFIG_LABEL))
                .withOptionalRelationshipType((String) config.get(CONFIG_RELATIONSHIP))
                .withOptionalWeightsFromProperty(
                        (String) config.get(CONFIG_PROPERTY),
                        (double) config.getOrDefault(CONFIG_DEFAULT_VALUE, 1.0))
                .withExecutorService(Pools.DEFAULT)
                .load(HeavyGraphFactory.class);

        final long[] path = new ShortestPathDijkstra(graph).compute(
                startNodeId.getId(),
                endNodeId.getId());

        LongStream.of(path).mapToObj(ShortestPathNode::new).forEach(System.out::println);

        return LongStream.of(path).mapToObj(ShortestPathNode::new);
    }

    public static class ShortestPathNode {
        public final Long nodeId;

        public ShortestPathNode(Long nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public String toString() {
            return "ShortestPathNode{" +
                    "nodeId=" + nodeId +
                    '}';
        }
    }
}
