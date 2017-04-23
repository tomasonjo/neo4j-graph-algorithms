package org.neo4j.graphalgo.algo;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntScatterMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphalgo.DijkstraProc;
import org.neo4j.graphalgo.UnionFindProc;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;

/**
 * @author mknblch
 */
public class ShortestPathIntegrationTest {

    private static GraphDatabaseAPI db;

    @BeforeClass
    public static void setup() throws KernelException {
        String createGraph =
                "CREATE (nA:Node{type:'start'})\n" + // start
                "CREATE (nB1:Node)\n" +
                "CREATE (nC1:Node)\n" +
                "CREATE (nD1:Node)\n" +
                "CREATE (nB2:Node)\n" +
                "CREATE (nC2:Node)\n" +
                "CREATE (nD2:Node)\n" +
                "CREATE (nB3:Node)\n" +
                "CREATE (nC3:Node)\n" +
                "CREATE (nD3:Node)\n" +
                "CREATE (nX:Node{type:'end'})\n" + // end
                "CREATE\n" +

                // sum: 14.0
                "  (nA)-[:TYPE {cost:2.0}]->(nB1),\n" +
                "  (nB1)-[:TYPE {cost:3.0}]->(nC1),\n" +
                "  (nC1)-[:TYPE {cost:4.0}]->(nD1),\n" +
                "  (nD1)-[:TYPE {cost:5.0}]->(nX),\n" +
                // sum: 8.0
                "  (nA)-[:TYPE {cost:2.0}]->(nB2),\n" +
                "  (nB2)-[:TYPE {cost:2.0}]->(nC2),\n" +
                "  (nC2)-[:TYPE {cost:2.0}]->(nD2),\n" +
                "  (nD2)-[:TYPE {cost:2.0}]->(nX),\n" +
                // sum: 4.0
                "  (nA)-[:TYPE {cost:1.0}]->(nB3),\n" +
                "  (nB3)-[:TYPE {cost:1.0}]->(nC3),\n" +
                "  (nC3)-[:TYPE {cost:1.0}]->(nD3),\n" +
                "  (nD3)-[:TYPE {cost:1.0}]->(nX)";


        db = (GraphDatabaseAPI)
                new TestGraphDatabaseFactory()
                        .newImpermanentDatabaseBuilder()
                        .newGraphDatabase();
        try (Transaction tx = db.beginTx()) {
            db.execute(createGraph).close();
            tx.success();
        }

        db.getDependencyResolver()
                .resolveDependency(Procedures.class)
                .registerProcedure(DijkstraProc.class);
    }

    @Test
    public void testDijkstra() throws Exception {
        db.execute(
                "MATCH (start:Node{type:'start'}), (end:Node{type:'end'}) " +
                        "CALL algo.dijkstra(start, end, {property:'cost'}) YIELD nodeId " +
                        "RETURN nodeId")
                .accept((Result.ResultVisitor<Exception>) row -> {
                    System.out.println(row.getNumber("nodeId"));
                    return false;
                });
    }


}
