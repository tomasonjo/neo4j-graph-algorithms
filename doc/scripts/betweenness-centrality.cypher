// tag::create-sample-graph[]

CREATE (nAlice:User {id:'Alice'})
,(nBridget:User {id:'Bridget'})
,(nCharles:User {id:'Charles'})
,(nDoug:User {id:'Doug'})
,(nMark:User {id:'Mark'})
,(nMichael:User {id:'Michael'})
CREATE (nAlice)-[:MANAGE]->(nBridget)
,(nAlice)-[:MANAGE]->(nCharles)
,(nAlice)-[:MANAGE]->(nDoug)
,(nMark)-[:MANAGE]->(nAlice)
,(nCharles)-[:MANAGE]->(nMichael);

// end::create-sample-graph[]

// tag::stream-sample-graph[]

CALL algo.betweenness.stream('User','MANAGE',{direction:'out'}) 
YIELD nodeId, centrality 
RETURN nodeId,centrality order by centrality desc limit 20;

// tag::stream-sample-graph[]

// tag::write-sample-graph[]

CALL algo.betweenness('User','MANAGE', {direction:'out',write:true, writeProperty:'centrality'}) 
YIELD nodes, minCentrality, maxCentrality, sumCentrality, loadMillis, computeMillis, writeMillis;

// end::write-sample-graph[]
