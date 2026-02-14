# Neo4j:

## Notes:
- In Cypher, labels, property keys, and variables are case-sensitive. Cypher keywords are not case-sensitive.
- Neo4j best practices include: Name labels using CamelCase. Name property keys and variables using camelCase. Use UPPERCASE for Cypher keywords.
- By default, the direction of the edge is from left to right. MERGE (p)-[:ACTED_IN]-(m)
- If you try to delete a node which have relationships with other nodes and you run this query - MATCH (p:Person {name: 'Jane Doe'}) DELETE p. It will give error. To delete a node, you should perform Detatch Delete because it also deletes teh relationship associated with that particular node - MATCH (p:Person {name: 'Jane Doe'}) DETACH DELETE p
- This deletes all nodes from the databse - MATCH (n) DETACH DELETE n



# Read data Queries

## Notes:
- You should never remove the property that is used as the primary key for a node.

## Queries:
- MATCH (u:User)-[r:RATED]->(m:Movie) WHERE u.name = "Mr. Jason Love" RETURN u, r, m
- match (m:Movie) where m.title = "Toy Story" return m
- MATCH (u:User)-[r:RATED]->(m:Movie) WHERE u.name = "Mr. Jason Love" RETURN u.name as name, r.rating, m.title
- MATCH (p:Person) WHERE p.name = 'Tom Hanks' OR p.name = 'Rita Wilson' RETURN p.name, p.born
- MATCH (p:Person {name: 'Tom Hanks'})-[:ACTED_IN]->(m) RETURN m.title
- /// not equals to operator
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE p.name <> 'Tom Hanks' AND m.title = 'Captain Phillips' RETURN p.name
- MATCH (p)-[:ACTED_IN]->(m) WHERE p:Person AND m:Movie AND m.title='The Matrix' RETURN p.name
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE 2000 <= m.released <= 2003 RETURN p.name, m.title, m.released
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE p.name='Jack Nicholson' AND m.tagline IS NOT NULL RETURN m.title, m.tagline

- //// Three ways of Filtering by partial strings - STARTS WITH, ENDS WITH, and CONTAINS - all of them are case-sensitive
- MATCH (p:Person)-[:ACTED_IN]->() WHERE p.name STARTS WITH 'Michael' RETURN p.name
- /// Solution for above problem
- MATCH (p:Person)-[:ACTED_IN]->() WHERE toLower(p.name) STARTS WITH 'michael' RETURN p.name
- /// Filtering using lists
- MATCH (p:Person) WHERE p.born IN [1965, 1970, 1975] RETURN p.name, p.born
- /// Filtering with existing lists
- MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) WHERE  'Neo' IN r.roles AND m.title='The Matrix' RETURN p.name, r.roles
- /// setting multiple queries inline
- MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) WHERE p.name = 'Michael Caine' AND m.title = 'The Dark Knight' SET r.roles = ['Alfred Penny'], m.released = 2008 RETURN p, r, m
- // order query
- MATCH (p:Person) WHERE p.born IS NOT NULL RETURN p.name AS name, p.born AS birthDate ORDER BY p.born DESC
- MATCH (p:Person)-[:DIRECTED | ACTED_IN]->(m:Movie) WHERE p.name = 'Tom Hanks' OR p.name = 'Keanu Reeves' RETURN  m.year, m.title ORDER BY m.year DESC, m.title AESC
- // Limiting the result
- MATCH (m:Movie) WHERE m.released IS NOT NULL RETURN m.title AS title, m.released AS releaseDate ORDER BY m.released DESC LIMIT 100
- // Skiping some results
- MATCH (p:Person) WHERE p.born.year = 1980 RETURN  p.name as name, p.born AS birthDate ORDER BY p.born SKIP 40 LIMIT 10
- // return distinct values
- MATCH (p:Person)-[:DIRECTED | ACTED_IN]->(m:Movie) WHERE p.name = 'Tom Hanks' RETURN DISTINCT m.title, m.released ORDER BY m.title
- MATCH (m:Movie)<-[:ACTED_IN]-(p:Person) WHERE m.title CONTAINS 'Toy Story' AND p.died IS NULL RETURN 'Movie: ' + m.title AS movie, p.name AS actor, p.born AS dob, date().year - p.born.year AS ageThisYear
- // Can create list while returning data
- MATCH (p:Person) RETURN p.name, [p.born, p.died] AS lifeTime LIMIT 10








# Create/Delete data Queries

## Notes:
- MERGE will only create the pattern if it doesn’t already exist. Merge works like this - get this node/relationship and if not available, create the node/relationship.
- 

## Queries:
- Merge (m:Movie {title:"GOT"}) set m.year = 2014 return m
- /// Multi line query for creating nodes and relations
- MERGE (m:Movie {title: "Arthur the King"})
  MERGE (u:User {name: "Adam"}) 
  MERGE (u)-[r:RATED {rating: 5}]->(m) RETURN u, r, m
- /// single line query
- MERGE (p:Person {name: 'Emily Blunt'})-[:ACTED_IN]->(m:Movie {title: 'A Quiet Place'}) RETURN p, m
- // Removing property 
- MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) WHERE p.name = 'Michael Caine' AND m.title = 'The Dark Knight' REMOVE r.roles
RETURN p, r, m
- /// Alternative way of removing is to set the property null
- MATCH (p:Person) WHERE p.name = 'Gene Hackman' SET p.born = null RETURN p
- MATCH (p:Person {name: 'Jane Doe'})-[r:ACTED_IN]->(m:Movie {title: 'The Matrix'}) DELETE r RETURN p, m
- MATCH (p:Person {name: 'Jane Doe'}) DETACH DELETE p
- /// add new label to the node
- MATCH (p:Person {name: 'Jane Doe'}) SET p:Developer RETURN p
- /// remove label from the node
- MATCH (p:Person {name: 'Jane Doe'}) REMOVE p:Developer RETURN p












# Advanced Concepts:

## Notes:
- If you transform a string property during a query, such as toUpper() or toLower(), the query engine turns off the use of the index. With any query, you can always check if an index will be used by prefixing the query with EXPLAIN. It only produces the execution plan of the query. 
eg - EXPLAIN MATCH (m:Movie)
WHERE  m.title STARTS WITH 'Toy Story'
RETURN m.title, m.released

- You can use the PROFILE keyword to show the total number of rows retrieved from the graph in the query. It provides both the execution plan and the db hits when the query executes.
eg - PROFILE MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
WHERE  p.name = 'Tom Hanks'
AND exists {(p)-[:DIRECTED]->(m)}
RETURN m.title

- In general, using a single MATCH clause will perform better than multiple MATCH clauses. This is because relationship uniqueness (in a pattern, one edge should be visited/traversed only once in one path) is enforced so there are fewer relationships traversed.
eg:-
Multiple match clause:
MATCH (a:Person)-[:ACTED_IN]->(m:Movie)
WHERE m.year > 2000
MATCH (m)<-[:DIRECTED]-(d:Person)
RETURN a.name, m.title, d.name

Single Match clause:
MATCH (a:Person)-[:ACTED_IN]->(m:Movie),
      (m)<-[:DIRECTED]-(d:Person)
WHERE m.year > 2000
RETURN a.name, m.title, d.name

Another way of writing single clause:
MATCH (a:Person)-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person)
WHERE m.year > 2000
RETURN a.name, m.title, d.name




## ON CREATE SET, ON MATCH SET
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

## OPTIONAL MATCH
- OPTIONAL MATCH matches patterns with your graph, just like MATCH does. The difference is that if no matches are found, OPTIONAL MATCH will use nulls for missing parts of the pattern. OPTIONAL MATCH could be considered the Cypher equivalent of the outer join in SQL.
- MATCH (m:Movie) WHERE m.title = "Kiss Me Deadly"
  MATCH (m)-[:IN_GENRE]->(g:Genre)<-[:IN_GENRE]-(rec:Movie)
  OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor)-[:ACTED_IN]->(rec)
  RETURN rec.title, a.name

## Map Projections to Return Data
- // Return every property after removing node internal values like labels info.
- MATCH (p:Person) WHERE p.name CONTAINS "Thomas" RETURN p { .* } AS person ORDER BY p.name ASC
- // Return specific property values
- MATCH (p:Person) WHERE p.name CONTAINS "Thomas" RETURN p { .name, .born } AS person ORDER BY p.name
- // Can also add additional values while returning the data
- MATCH (m:Movie)<-[:DIRECTED]-(d:Director) WHERE d.name = 'Woody Allen' RETURN m {.*, favorite: true} AS movie

## Conditionally changing data returned
- MATCH (m:Movie)<-[:ACTED_IN]-(p:Person)
WHERE p.name = 'Henry Fonda'
RETURN m.title AS movie,
CASE
WHEN m.year < 1940 THEN 'oldies'
WHEN 1940 <= m.year < 1950 THEN 'forties'
WHEN 1950 <= m.year < 1960 THEN 'fifties'
WHEN 1960 <= m.year < 1970 THEN 'sixties'
WHEN 1970 <= m.year < 1980 THEN 'seventies'
WHEN 1980 <= m.year < 1990 THEN 'eighties'
WHEN 1990 <= m.year < 2000 THEN 'nineties'
ELSE  'two-thousands'
END
AS timeFrame

## Aggregation functions:
### Count()
- Cypher has a count() function that you can use to perform a count of nodes, relationships, paths, rows during query processing. When you aggregate in a Cypher statement, this means that the query must process all patterns in the MATCH clause to complete the aggregation to either return results or perform the next part of the query.
- In cypher, results are grouped automically. You dont have to explicitly tell that
- If you specify count(n), the graph engine calculates the number of non-null occurrences of n. If you specify count(*), the graph engine calculates the number of rows retrieved, including those with null values.
- // Grouping happens with (actorname, directorName)
- MATCH (a:Person)-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person)
RETURN a.name AS actorName,
d.name AS directorName,
count(*) AS numMovies
ORDER BY numMovies DESC

## Using collect() to create a list
- // Can also use DISTINCT keyword inside collect() if you want to store distinct entries
- MATCH (a:Person)-[:ACTED_IN]->(m:Movie)
RETURN a.name AS actor,
count(*) AS total,
collect(m.title) AS movies
ORDER BY total DESC LIMIT 10
- // Accessing element of the list
- MATCH (a:Person)-[:ACTED_IN]->(m:Movie)
RETURN m.title AS movie,
collect(a.name)[0] AS castMember,
size(collect(a.name)) as castSize
- // Return list containing elements fro index 2 till end
- MATCH (a:Person)-[:ACTED_IN]->(m:Movie)
RETURN m.title AS movie,
collect(a.name)[2..] AS castMember,
size(collect(a.name)) as castSize
- // List Comprehension - You can create a list by evaluating an expression that tests for list inclusion
- MATCH (m:Movie)
RETURN m.title as movie,
[x IN m.countries WHERE x CONTAINS 'USA' OR x CONTAINS 'Germany']
AS country LIMIT 500
- // Pattern Comprehension : syntax -> [<pattern> | value]
- MATCH (m:Movie)
WHERE m.year = 2015
RETURN m.title,
[(dir:Person)-[:DIRECTED]->(m) | dir.name] AS directors,
[(actor:Person)-[:ACTED_IN]->(m) | actor.name] AS actors
- MATCH (a:Person {name: 'Tom Hanks'})
RETURN [(a)-->(b:Movie)
WHERE b.title CONTAINS "Toy" | b.title + ": " + b.year] AS movies

## Date and Time
- MERGE (x:Test {id: 1})
SET
x.date = date(),
x.datetime = datetime(),
x.timestamp = timestamp(),
x.date1 = date('2022-04-08'),
x.date2 = date('2022-09-20'),
x.datetime1 = datetime('2022-02-02T15:25:33'),
x.datetime2 = datetime('2022-02-02T22:06:12')
RETURN x
- MATCH (x:Test {id: 1})
RETURN x.date.day, x.date.year,
x.datetime.year, x.datetime.hour,
x.datetime.minute
- MATCH (x:Test)
RETURN duration.inDays(x.date1,x.date2).days/.days/.minutes

## Graph Traversal






# Practice Queries
1. find all people who wrote a movie, acted in the movie but not direct that same movie?
- MATCH (p:Person)-[:WROTE]->(m:Movie), (p)-[:ACTED_IN]->(m) WHERE NOT exists((p)-[:DIRECTED]->(m)) RETURN p.name, m.title

2. Find person nodes who have actor and director label also
- MATCH (p:Person) WHERE p.born.year > 1960 AND p:Actor AND p:Director RETURN p.name, p.born, labels(p)

3. Find person who acted and directed in the same movie
- MATCH (p:Person)-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(p) WHERE  p.born.year > 1960 RETURN p.name, p.born, labels(p), m.title

4. Find all relationships between person and movie node
- MATCH (p:Person)-[r]->(m:Movie) WHERE  p.name = 'Tom Hanks' RETURN m.title AS movie, type(r) AS relationshipType

5. Find the directors of horror movies released in year 2000?
- /// This is multi-line query which improves readability. Can also use single line query like in Q3
- MATCH (d:Director)-[:DIRECTED]->(m:Movie)     
(m)-[:IN_GENRE]->(g:Genre) 
WHERE m.year = 2000 AND g.name = horror
RETURN d.name

6. Find person who act and direct and born in 1950

- MATCH (p:Person)
WHERE p:Actor AND p:Director
AND 1950 <= p.born.year < 1960
RETURN p.name, labels(p), p.born

7. 