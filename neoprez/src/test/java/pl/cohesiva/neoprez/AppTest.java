package pl.cohesiva.neoprez;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

public class AppTest {

	private static GraphDatabaseService databaseService;
	
	public enum Relation implements RelationshipType {
		HAS
	}
	
	@BeforeClass
	public static void beforeClass() {
		databaseService = new GraphDatabaseFactory().newEmbeddedDatabase("db");
	}
	
	@AfterClass
	public static void afterClass() {
		Node referenceNode = databaseService.getNodeById(0);
		TraversalDescription traversalDescription = Traversal.description();
		traversalDescription.depthFirst();
		traversalDescription.evaluator(Evaluators.excludeStartPosition());
		Traverser traverse = traversalDescription.traverse(referenceNode);
		final Iterable<Relationship> relationships = traverse.relationships();
		final Iterable<Node> nodes = traverse.nodes();
		final Transaction tx = databaseService.beginTx();
		try {
			for(Relationship rel : relationships) {
				rel.delete();
			}			
			for(Node node : nodes) {
				node.delete();
			}
			tx.success();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			tx.finish();
		}		
		
		databaseService.shutdown();
	}

	@Test
	public void canCreateAndReadObjectWithPropertiesAssignedToIt() {
		final String NAME_PROPERTY = "name";
		final String AGE_PROPERTY = "age";
		
		final String NAME1 = "Leszek";
		final int AGE1 = 39;
		
		final Transaction tx = databaseService.beginTx();
		final Node referenceNode = databaseService.getNodeById(0);
		Node objectNode = null;
		try {
			objectNode = databaseService.createNode();
			objectNode.setProperty(NAME_PROPERTY, NAME1);
			objectNode.setProperty(AGE_PROPERTY, AGE1);
			referenceNode.createRelationshipTo(objectNode, Relation.HAS);
			tx.success();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			tx.finish();
		}
		
		
		Node accquiredNode = referenceNode.getRelationships(Relation.HAS, Direction.OUTGOING).iterator().next().getEndNode();
		
		assertNotNull(accquiredNode);
		assertEquals(accquiredNode.getProperty(NAME_PROPERTY), NAME1);
		assertEquals(accquiredNode.getProperty(AGE_PROPERTY), AGE1);
	}
	
}
