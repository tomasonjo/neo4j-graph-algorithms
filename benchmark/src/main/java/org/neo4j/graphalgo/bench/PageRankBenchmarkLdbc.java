package org.neo4j.graphalgo.bench;

import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.utils.Pools;
import org.neo4j.graphalgo.impl.PageRank;
import org.neo4j.graphdb.Direction;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Threads(1)
@Fork(value = 1, jvmArgs = {"-Xms4g", "-Xmx4g"})
@Warmup(iterations = 2)
@Measurement(iterations = 4)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class PageRankBenchmarkLdbc {

    @Param({"LIGHT", "HEAVY", "VIEW"})
    GraphImpl graph;

    @Param({"5", "20"})
    int iterations;

   @Param({"10000", "2000000000"})
   int parallelBatchSize;

    private GraphDatabaseAPI db;
    private Graph grph;

    @Setup
    public void setup() throws KernelException, IOException {
        db = LdbcDownloader.openDb();
        grph = new GraphLoader(db, Pools.DEFAULT).withDirection(Direction.OUTGOING).load(this.graph.impl);
    }

    @TearDown
    public void shutdown() {
        db.shutdown();
        Pools.DEFAULT.shutdownNow();
    }

    @Benchmark
    public double[] run() throws Exception {
        final Graph graph = grph;
        return new PageRank(
                Pools.DEFAULT,
                Pools.getNoThreadsInDefaultPool(),
                parallelBatchSize,
                graph,
                graph,
                graph,
                graph,
                0.85)
                .compute(iterations)
                .getPageRank();
    }
}
