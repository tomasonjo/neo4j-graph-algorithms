package org.neo4j.graphalgo.impl;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntStack;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.utils.AtomicDoubleArray;
import org.neo4j.graphalgo.core.utils.ParallelUtil;
import org.neo4j.graphalgo.core.utils.container.Paths;
import org.neo4j.graphdb.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Implements Betweenness Centrality for unweighted graphs
 * as specified in <a href="http://www.algo.uni-konstanz.de/publications/b-fabc-01.pdf">this paper</a>
 * using node-partitioning
 *
 * @author mknblch
 */
public class ParallelBetweennessCentrality {

    // the graph
    private final Graph graph;
    // AI counts up for every node until nodeCount is reached
    private volatile AtomicInteger nodeQueue = new AtomicInteger();
    // atomic double array which supports only atomic-add
    private final AtomicDoubleArray centrality;
    // the node count
    private final int nodeCount;
    // global executor service
    private final ExecutorService executorService;
    // number of threads to spawn
    private final int concurrency;

    /**
     * constructs a parallel centrality solver
     *
     * @param graph the graph iface
     * @param scaleFactor factor used to scale up doubles to integers in AtomicDoubleArray
     * @param executorService the executor service
     * @param concurrency desired number of threads to spawn
     */
    public ParallelBetweennessCentrality(Graph graph, double scaleFactor, ExecutorService executorService, int concurrency) {
        this.graph = graph;
        this.nodeCount = graph.nodeCount();
        this.executorService = executorService;
        this.concurrency = concurrency;
        this.centrality = new AtomicDoubleArray(graph.nodeCount(), scaleFactor);
    }

    /**
     * compute centrality
     * @return itself for method chaining
     */
    public ParallelBetweennessCentrality compute() {
        nodeQueue.set(0);
        final ArrayList<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            futures.add(executorService.submit(new BCTask()));
        }
        ParallelUtil.awaitTermination(futures);
        return this;
    }

    /**
     * get the centrality array
     * @return array with centrality
     */
    public AtomicDoubleArray getCentrality() {
        return centrality;
    }

    /**
     * iterate over each result until every node has
     * been visited or the consumer returns false
     *
     * @param consumer the result consumer
     */
    public void forEach(BetweennessCentrality.ResultConsumer consumer) {
        for (int i = graph.nodeCount() - 1; i >= 0; i--) {
            if (!consumer.consume(graph.toOriginalNodeId(i), centrality.get(i))) {
                return;
            }
        }
    }

    /**
     * emit the result stream
     * @return stream if Results
     */
    public Stream<BetweennessCentrality.Result> resultStream() {
        return IntStream.range(0, graph.nodeCount())
                .mapToObj(nodeId ->
                        new BetweennessCentrality.Result(
                                graph.toOriginalNodeId(nodeId),
                                centrality.get(nodeId)));
    }

    /**
     * a BCTask takes one element from the nodeQueue as long as
     * it is lower then nodeCount and calculates it's centrality
     */
    private class BCTask implements Runnable {

        private final Paths paths;
        private final IntStack stack;
        private final IntArrayDeque queue;
        private final double[] delta;
        private final int[] sigma;
        private final int[] distance;

        private BCTask() {
            this.paths = new Paths();
            this.stack = new IntStack();
            this.queue = new IntArrayDeque();
            this.sigma = new int[nodeCount];
            this.distance = new int[nodeCount];
            this.delta = new double[nodeCount];
        }

        @Override
        public void run() {
            for (;;) {
                reset();
                final int startNodeId = nodeQueue.getAndIncrement();
                if (startNodeId >= nodeCount) {
                    return;
                }
                sigma[startNodeId] = 1;
                distance[startNodeId] = 0;
                queue.addLast(startNodeId);
                while (!queue.isEmpty()) {
                    int node = queue.removeLast();
                    stack.push(node);
                    graph.forEachRelationship(node, Direction.OUTGOING, (source, target, relationId) -> {
                        if (distance[target] < 0) {
                            queue.addLast(target);
                            distance[target] = distance[node] + 1;
                        }
                        if (distance[target] == distance[node] + 1) {
                            sigma[target] += sigma[node];
                            paths.append(target, node);
                        }
                        return true;
                    });
                }

                while (!stack.isEmpty()) {
                    int node = stack.pop();
                    paths.forEach(node, v -> {
                        delta[v] += (double) sigma[v] / (double) sigma[node] * (delta[node] + 1.0);
                        if (node != startNodeId) {
                            centrality.add(node, delta[node]);
                        }
                        return true;
                    });
                }
            }
        }

        /**
         * reset local state
         */
        private void reset() {
            paths.clear();
            stack.clear();
            queue.clear();
            Arrays.fill(sigma, 0);
            Arrays.fill(delta, 0);
            Arrays.fill(distance, -1);
        }
    }
}
