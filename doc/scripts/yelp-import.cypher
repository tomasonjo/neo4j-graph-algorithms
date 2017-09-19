// tag::load-schema[]
CALL apoc.schema.assert(
{Category:['name']},
{Business:['id'],User:['id'],Review:['id']});
// end::load-schema[]

// tag::load-business[]
CALL apoc.periodic.iterate("
CALL apoc.load.json('file:///dataset/business.json') YIELD value RETURN value
","
MERGE (b:Business{id:value.business_id})
SET b += apoc.map.clean(value, ['attributes','hours','business_id','categories','address','postal_code'],[]) 
WITH b,value.categories as categories
UNWIND categories as category
MERGE (c:Category{id:category})
MERGE (b)-[:IN_CATEGORY]->(c)
",{batchSize: 10000, iterateList: true});
// end::load-business[]

// tag::load-tip[]

CALL apoc.periodic.iterate("
CALL apoc.load.json('file:///dataset/tip.json') YIELD value RETURN value
","
MATCH (b:Business{id:value.business_id})
MERGE (u:User{id:value.user_id})
MERGE (u)-[:TIP{date:value.date,likes:value.likes}]->(b)
",{batchSize: 20000, iterateList: true});
// end::load-tip[]

// tag::load-review[]
CALL apoc.periodic.iterate("
CALL apoc.load.json('file:///dataset/review.json') 
YIELD value RETURN value
","
MERGE (b:Business{id:value.business_id})
MERGE (u:User{id:value.user_id})
MERGE (r:Review{id:value.review_id})
MERGE (u)-[:WROTE]->(r)
MERGE (r)-[:REVIEWS]->(b)
SET r += apoc.map.clean(value, ['business_id','user_id','review_id','text'],["0"]) 
",{batchSize: 10000, iterateList: true});
// end::load-review[]


// tag::load-user[]

CALL apoc.periodic.iterate("
CALL apoc.load.json('file:///dataset/user.json') 
YIELD value RETURN value
","
MERGE (u:User{id:value.user_id})
SET u += apoc.map.clean(value, ['friends','user_id'],[])
WITH u,value.friends as friends
UNWIND friends as friend
MERGE (u1:User{id:friend})
MERGE (u)-[:FRIEND]-(u1)
",{batchSize: 100, iterateList: true});
// end::load-user[]


// tag::cooccurence-graph[]

CALL apoc.periodic.iterate(
"MATCH (p1:User) WHERE size((p1)-[:WROTE]->()) > 5 RETURN p1",
"
MATCH (p1)-[:WROTE]->(r1)-->()<--(r2)<-[:WROTE]-(p2)
WHERE id(p1) < id(p2) AND size((p2)-[:WROTE]->()) > 10
WITH p1,p2,count(*) as coop, collect(r1.stars) as s1, collect(r2.stars) as s2 where coop > 10
WITH p1,p2, apoc.algo.cosineSimilarity(s1,s2) as cosineSimilarity WHERE cosineSimilarity > 0
MERGE (p1)-[s:SIMILAR_5_10]-(p2) SET s.weight = cosineSimilarity"
, {batchSize:100, parallel:false,iterateList:true});

// end::cooccurence-graph[]