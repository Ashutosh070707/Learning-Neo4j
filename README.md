## Neo4j Notes:
#### Notes:
- In Cypher, labels, property keys, and variables are case-sensitive. Cypher keywords are not case-sensitive.
- Neo4j best practices include: Name labels using CamelCase. Name property keys and variables using camelCase. Use UPPERCASE for Cypher keywords.
- By default, the direction of the edge is from left to right. MERGE (p)-[:ACTED_IN]-(m)
- If you try to delete a node which have relatiuonships with other nodes and you run this query - MATCH (p:Person {name: 'Jane Doe'}) DELETE p. It will give error. To delete a node, you should perform Detatch Delete - MATCH (p:Person {name: 'Jane Doe'}) DETACH DELETE p
- This deletes all nodes from the databse - MATCH (n) DETACH DELETE n



### Read data Queries
#### Notes:
- You should never remove the property that is used as the primary key for a node.

#### Queries:
- MATCH (u:User)-[r:RATED]->(m:Movie) WHERE u.name = "Mr. Jason Love" RETURN u, r, m
- match (m:Movie) where m.title = "Toy Story" return m
- MATCH (u:User)-[r:RATED]->(m:Movie) WHERE u.name = "Mr. Jason Love" RETURN u.name as name, r.rating, m.title
- MATCH (p:Person) WHERE p.name = 'Tom Hanks' OR p.name = 'Rita Wilson' RETURN p.name, p.born
- MATCH (p:Person {name: 'Tom Hanks'})-[:ACTED_IN]->(m) RETURN m.title
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE p.name <> 'Tom Hanks' AND m.title = 'Captain Phillips' RETURN p.name  // not equals to operator
- MATCH (p)-[:ACTED_IN]->(m) WHERE p:Person AND m:Movie AND m.title='The Matrix' RETURN p.name
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE 2000 <= m.released <= 2003 RETURN p.name, m.title, m.released
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE p.name='Jack Nicholson' AND m.tagline IS NOT NULL RETURN m.title, m.tagline

- //// Three ways of Filtering by partial strings - STARTS WITH, ENDS WITH, and CONTAINS
- MATCH (p:Person)-[:ACTED_IN]->() WHERE p.name STARTS WITH 'Michael' RETURN p.name    // STARTS WITH is case-sensitive
- MATCH (p:Person)-[:ACTED_IN]->() WHERE toLower(p.name) STARTS WITH 'michael' RETURN p.name // Solution for above problem
- MATCH (p:Person) WHERE p.born IN [1965, 1970, 1975] RETURN p.name, p.born  // Filtering usiong lists
- MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) WHERE  'Neo' IN r.roles AND m.title='The Matrix' RETURN p.name, r.roles  // Filtering with existing lists
- MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) WHERE p.name = 'Michael Caine' AND m.title = 'The Dark Knight' SET r.roles = ['Alfred Penny'], m.released = 2008 RETURN p, r, m    // setting multiple queries inline


### Create/Delete data Queries
#### Notes:
- MERGE will only create the pattern if it doesn’t already exist. Merge works like this - get this node/relationship and if not available, create the node/relationship.
- 

#### Queries:
- Merge (m:Movie {title:"GOT"}) set m.year = 2014 return m
- MERGE (m:Movie {title: "Arthur the King"})    // Multi line query for creating nodes and relations
MERGE (u:User {name: "Adam"}) 
MERGE (u)-[r:RATED {rating: 5}]->(m) RETURN u, r, m
- MERGE (p:Person {name: 'Emily Blunt'})-[:ACTED_IN]->(m:Movie {title: 'A Quiet Place'}) RETURN p, m   // single line query
- MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) WHERE p.name = 'Michael Caine' AND m.title = 'The Dark Knight' REMOVE r.roles
RETURN p, r, m   // Removing property 
- MATCH (p:Person) WHERE p.name = 'Gene Hackman' SET p.born = null RETURN p   // Alternative way of removing is to set the property null
- MATCH (p:Person {name: 'Jane Doe'})-[r:ACTED_IN]->(m:Movie {title: 'The Matrix'}) DELETE r RETURN p, m
- MATCH (p:Person {name: 'Jane Doe'}) DETACH DELETE p
- MATCH (p:Person {name: 'Jane Doe'}) SET p:Developer RETURN p    // add new label to the node
- MATCH (p:Person {name: 'Jane Doe'}) REMOVE p:Developer RETURN p   // remove label from the node


### Advanced Concepts:
#### ON CREATE SET, ON MATCH SET
- Used for customizing Merge behaviour
- MERGE (p:Person {name: 'McKenna Grace'}) 
ON CREATE SET p.createdAt = datetime()
ON MATCH SET p.updatedAt = datetime()
SET p.born = 2006
MERGE (m:Movie {title: 'Ghostbusters: Afterlife'})
MERGE (p)-[r:ACTED_IN]->(m)
ON CREATE SET r.roles = ['Phoebe'], r.firstAssigned = datetime()
ON MATCH SET r.lastUpdated = datetime()
RETURN p, r, m

#### Practice Queries
1. find all people who wrote a movie, acted in the movie but not direct that same movie?
- MATCH (p:Person)-[:WROTE]->(m:Movie), (p)-[:ACTED_IN]->(m) WHERE NOT exists((p)-[:DIRECTED]->(m)) RETURN p.name, m.title

2. Find person nodes who have actor and director label also
- MATCH (p:Person) WHERE p.born.year > 1960 AND p:Actor AND p:Director RETURN p.name, p.born, labels(p)

3. Find person who acted and directed in the same movie
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(p) WHERE  p.born.year > 1960 RETURN p.name, p.born, labels(p), m.title

4. Find all relationships between person and movie node
- MATCH (p:Person)-[r]->(m:Movie) WHERE  p.name = 'Tom Hanks' RETURN m.title AS movie, type(r) AS relationshipType

5. Find the directors of horror movies released in year 2000?
- // This is multi-line query which improves readability. Can also use single line query like in Q3
- MATCH (d:Director)-[:DIRECTED]->(m:Movie)     
(m)-[:IN_GENRE]->(g:Genre) 
WHERE m.year = 2000 AND g.name = horror
RETURN d.name

6. 