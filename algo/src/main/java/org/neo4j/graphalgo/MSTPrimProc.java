package org.neo4j.graphalgo;

import algo.Pools;
import com.carrotsearch.hppc.LongLongHashMap;
import com.carrotsearch.hppc.LongLongMap;
import com.carrotsearch.hppc.LongLongScatterMap;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.api.Weights;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.heavyweight.HeavyGraphFactory;
import org.neo4j.graphalgo.core.sources.BothRelationshipAdapter;
import org.neo4j.graphalgo.core.sources.BufferedWeightMap;
import org.neo4j.graphalgo.core.sources.LazyIdMapper;
import org.neo4j.graphalgo.core.utils.container.RelationshipContainer;
import org.neo4j.graphalgo.core.utils.container.UndirectedTree;
import org.neo4j.graphalgo.impl.MSTPrim;
import org.neo4j.graphalgo.impl.ShortestPathDijkstra;
import org.neo4j.graphalgo.results.MSTResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author mknblch
 */
public class MSTPrimProc {

    public static final String CONFIG_LABEL = "label";
    public static final String CONFIG_RELATIONSHIP = "relationship";
    public static final String CONFIG_PROPERTY = "relationship";
    public static final String CONFIG_DEFAULT_VALUE = "defaultValue";

    @Context
    public GraphDatabaseAPI api;

    @Context
    public Log log;

    public Stream<Result> mstPrim(
            @Name("startNode") Node startNode,
            @Name("property") String propertyName,
            @Name(value = "config", defaultValue = "{}") Map<String, Object> config) {

        LazyIdMapper idMapper = new LazyIdMapper();

        Weights weightMap = BufferedWeightMap.importer(api)
                .withIdMapping(idMapper)
                .withAnyDirection(true)
                .withOptionalLabel((String) config.get(CONFIG_LABEL))
                .withOptionalRelationshipType(CONFIG_RELATIONSHIP)
                .withWeightsFromProperty(propertyName, 1.0)
                .build();

        RelationshipContainer relationshipContainer = RelationshipContainer.importer(api)
                .withIdMapping(idMapper)
                .withDirection(Direction.BOTH)
                .withOptionalLabel((String) config.get(CONFIG_LABEL))
                .withOptionalRelationshipType(CONFIG_RELATIONSHIP)
                .build();

        int startNodeId = idMapper.toMappedNodeId(startNode.getId());
        UndirectedTree tree = new MSTPrim(idMapper,
                new BothRelationshipAdapter(relationshipContainer),
                weightMap).compute(startNodeId);

        LongLongMap map = new LongLongScatterMap();

        tree.forEachDFS(startNodeId, (sourceNodeId, targetNodeId, relationId) -> {
            map.put(sourceNodeId, targetNodeId);
            return true;
        });

        return StreamSupport.stream(map.spliterator(), false)
                .map(cursor -> new Result(cursor.key, cursor.value));
    }

    public static class Result {

        public final Long nodeId;

        public final Long targetNodeId;

        public Result(Long nodeId, Long targetNodeId) {
            this.nodeId = nodeId;
            this.targetNodeId = targetNodeId;
        }
    }
}
