// tag::create-sample-graph[]

CREATE (nAlice:User {id:'Alice'})
,(nBridget:User {id:'Bridget'})
,(nCharles:User {id:'Charles'})
,(nDoug:User {id:'Doug'})
,(nMark:User {id:'Mark'})
,(nMichael:User {id:'Michael'})
CREATE (nAlice)-[:FOLLOW]->(nBridget)
,(nAlice)-[:FOLLOW]->(nCharles)
,(nMark)-[:FOLLOW]->(nDoug)
,(nMark)-[:FOLLOW]->(nMichael)
,(nBridget)-[:FOLLOW]->(nMichael)
,(nDoug)-[:FOLLOW]->(nMark)
,(nMichael)-[:FOLLOW]->(nAlice)
,(nAlice)-[:FOLLOW]->(nMichael)
,(nBridget)-[:FOLLOW]->(nAlice)
,(nMichael)-[:FOLLOW]->(nBridget);

// end::create-sample-graph[]

// tag::write-sample-graph[]

CALL algo.scc('User','FOLLOW', {write:true,partitionProperty:'partition'})
YIELD loadMillis, computeMillis, writeMillis, setCount, maxSetSize, minSetSize;

// end::write-sample-graph[]