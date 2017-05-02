package org.neo4j.graphalgo;

import algo.Pools;
import com.carrotsearch.hppc.LongLongMap;
import com.carrotsearch.hppc.LongLongScatterMap;
import org.neo4j.graphalgo.api.RelationshipWeights;
import org.neo4j.graphalgo.core.sources.BothRelationshipAdapter;
import org.neo4j.graphalgo.core.sources.BufferedWeightMap;
import org.neo4j.graphalgo.core.sources.LazyIdMapper;
import org.neo4j.graphalgo.core.utils.ProgressTimer;
import org.neo4j.graphalgo.core.utils.container.RelationshipContainer;
import org.neo4j.graphalgo.core.utils.container.UndirectedTree;
import org.neo4j.graphalgo.impl.MSTPrim;
import org.neo4j.graphalgo.results.MSTPrimResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

    @Procedure("algo.mstprim.augment")

    public Stream<MSTPrimResult> mstPrim(
            @Name("startNode") Node startNode,
            @Name("property") String propertyName,
            @Name(value = "config", defaultValue = "{}") Map<String, Object> config) {

        LazyIdMapper idMapper = new LazyIdMapper();

        MSTPrimResult.Builder builder = MSTPrimResult.builder();

        ProgressTimer timer = ProgressTimer.start(builder::withLoadDuration);
        Future<BufferedWeightMap> weightMap = BufferedWeightMap.importer(api)
                .withIdMapping(idMapper)
                .withAnyDirection(true)
                .withOptionalLabel((String) config.get(CONFIG_LABEL))
                .withOptionalRelationshipType(CONFIG_RELATIONSHIP)
                .withWeightsFromProperty(propertyName, 1.0)
                .delay(Pools.createDefaultPool());

        Future<RelationshipContainer> relationshipContainer = RelationshipContainer.importer(api)
                .withIdMapping(idMapper)
                .withDirection(Direction.BOTH)
                .withOptionalLabel((String) config.get(CONFIG_LABEL))
                .withOptionalRelationshipType(CONFIG_RELATIONSHIP)
                .delay(Pools.createDefaultPool());


        RelationshipContainer container;
        BufferedWeightMap weights;
        int startNodeId;

        try {
            container = relationshipContainer.get();
            weights = weightMap.get();
            startNodeId = idMapper.toMappedNodeId(startNode.getId());
            timer.stop();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        timer = ProgressTimer.start(builder::withEvalDuration);
        final MSTPrim mstPrim = new MSTPrim(idMapper, new BothRelationshipAdapter(container), weights)
                .compute(startNodeId);
        timer.stop();

        timer = ProgressTimer.start(builder::withWriteDuration);
        mstPrim.getMinimumSpanningTree()
                .forEachBFS(startNodeId, (source, target, rel) -> {


                    return true;
                });

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
