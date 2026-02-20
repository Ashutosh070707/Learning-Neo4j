package com.neo4j.app;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import org.neo4j.driver.RoutingControl;
import org.neo4j.driver.SessionConfig;

import com.neo4j.app.models.MoiveActors;
import com.neo4j.app.models.MovieActorDetails;
import com.neo4j.app.models.Person;

// Import the driver

public class App {
    public static void main(String[] args) {
        AppProperties.loadProperties();

        // /////////////////////////////////////////////////////////////////////////////

        // ********* Create a new Neo4j driver instance

        var driver = GraphDatabase.driver(
                System.getProperty("NEO4J_URI"),
                AuthTokens.basic(
                        System.getProperty("NEO4J_USERNAME"),
                        System.getProperty("NEO4J_PASSWORD")));

        // Verify the connection

        driver.verifyConnectivity();
        System.out.println("Connection to Neo4j successful!");

        // // Execute a Cypher query
        // var result = driver.executableQuery(
        // "return count {()} as count"
        // ).execute();

        // var records = result.records();
        // var first = records.get(0);
        // System.out.println(first.get("count"));

        // Use this if you want to specify the database on which you want to
        // run the cypher query

        // var result = driver.executableQuery(
        // "Match (n) return count(n) as count"
        // ).withConfig(QueryConfig.builder().withDatabase("neo4j").build()).execute();

        // Close the connection

        // driver.close();
        // System.out.println("Driver is closed!!");

        // /////////////////////////////////////////////////////////////////////////////

        // Can also use try-with-resources to automatically release the
        // resources and close the driver

        // try (
        // var driver = GraphDatabase.driver(
        // System.getProperty("NEO4J_URI"),
        // AuthTokens.basic(
        // System.getProperty("NEO4J_USERNAME"),
        // System.getProperty("NEO4J_PASSWORD")))) {
        // driver.verifyConnectivity();

        // var result = driver.executableQuery(
        // "RETURN COUNT {()} AS count")
        // .withConfig(QueryConfig.builder()
        // .withDatabase(System.getProperty("NEO4J_DATABASE", "neo4j"))
        // .build())
        // .execute();
        // }

        // /////////////////////////////////////////////////////////////////////////////

        // ********* Executing Cypher statements

        // final String cypher = """
        // MERGE (p:Person {name: $name})
        // MERGE (m:Movie {title: 'My Love Story'})
        // MERGE (p)-[r:ACTED_IN {role: $role}]->(m)
        // RETURN m.title as title, r.role as role
        // """;

        // final String name = "Ashutosh";
        // final String role = "Sulakshana's Husband";

        // var result = driver.executableQuery(cypher).withParameters(Map.of("name",
        // name, "role", role)).execute();

        // var records = result.records();
        // var summary = result.summary();
        // var keys = result.keys();
        // System.out.println(records);
        // System.out.println(summary);
        // System.out.println(keys);
        // records.forEach(r -> {
        // System.out.println(r.get("title"));
        // System.out.println(r.get("role"));
        // });

        // Executing query in Read mode

        // var result = driver.executableQuery(cypher)
        // .withParameters(Map.of("name", name))
        // .withConfig(QueryConfig.builder()
        // .withRouting(RoutingControl.READ) // **********
        // .withDatabase("neo4j") // Optional: specify database name
        // .build())
        // .execute();

        // driver.close();
        // System.out.println("Driver is closed!!");

        // /////////////////////////////////////////////////////////////////////////////

        // ********* Mapping Results to Java Objects

        // final String personCypher = """
        // Match (n:Person) return n;
        // """;

        // var result1 = driver.executableQuery(personCypher).execute();

        // var persons = result1.records().stream().map(record -> new Person(
        // record.get("n").get("name").asString()))
        // .toList();

        // Can do this also
        // List<Person> persons = driver.executableQuery(personCypher)
        // .execute()
        // .records()
        // .stream()
        // .map(record -> {
        // // 1. Get the Neo4j Node returned as 'n'
        // var/Node node = record.get("n").asNode();

        // // 2. Extract the properties and construct your Java Record
        // return new Person(
        // node.get("name").asString()
        // );
        // })
        // .toList();

        // var records = result.records();
        // var summary = result.summary();
        // var keys = result.keys();
        // System.out.println(records);
        // System.out.println(summary);
        // System.out.println(keys);
        // System.out.println(persons);

        // final String movieActorsCypher = """
        // match (m:Movie {title: $title})
        // match (n:Person)-[r:ACTED_IN]->(m)
        // return n.name as name, r.role as role;
        // """;

        // final String title = "My Love Story";

        // var actorDetailList =
        // driver.executableQuery(movieActorsCypher).withParameters(Map.of("title",
        // title)).execute().records().stream().map(record -> {
        // return new MovieActorDetails(record.get("name").asString(),
        // record.get("role").asString());
        // }).toList();

        // var moiveActors = new MoiveActors(title, actorDetailList);
        // System.out.println(moiveActors);

        // driver.close();
        // System.out.println("Driver is closed!!");

        // /////////////////////////////////////////////////////////////////////////////

        // ************* Transaction management

        // try (var session = driver.session(
        //         SessionConfig.builder().withDatabase("neo4j").build())) {
        //     // Call transaction functions here
        //     String name = "";
        //     int age=0;
        //     var count = session.executeWrite(tx -> createPerson(tx, name, age));
        // }


        driver.close();
        System.out.println("Driver is closed!!");

    }

    public static int createPerson(TransactionContext tx, String name, int age) { // (1)
        var result = tx.run("""
                CREATE (p:Person {name: $name, age: $age}) RETURN p
                """, Map.of("name", name, "age", age)); // (2)

        // The result must be consumed within the transaction function.
        return result.list().size();
    }

    public static void transferFunds(TransactionContext tx, String fromAccount, String toAccount, double amount) {
        tx.run(
                "MATCH (a:Account {id: $from_}) SET a.balance = a.balance - $amount",
                Map.of("from_", fromAccount, "amount", amount));
        
        tx.run(
                "MATCH (a:Account {id: $to_}) SET a.balance = a.balance + $amount",
                Map.of("to_", toAccount, "amount", amount));
    }
}
