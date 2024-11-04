package unit;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.event.core.EventNodeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class EventNodeTests {
    static class TestEvent implements Event {}

    private EventNodeImpl<Event> rootNode;

    @BeforeEach
    void setUp() {
        rootNode = new EventNodeImpl<>("root", Event.class, null);
    }

    @Test
    @DisplayName("Node should initialize with correct name and no children or parents")
    void testRootNodeCreation() {
        assertEquals("root", this.rootNode.name());
        assertTrue(this.rootNode.children().isEmpty(), "Root node should have no children initially.");
        assertFalse(this.rootNode.hasParents(), "Root node should not have any parents.");
    }

    @Test
    @DisplayName("Adding a child node should update the root's children list and child’s parent count")
    void testAddChildNode() {
        EventNodeImpl<Event> childNode = new EventNodeImpl<>("child", Event.class, null);
        this.rootNode.addChildNode(childNode);

        assertEquals(1, rootNode.children().size(), "Root node should have one child.");
        assertTrue(this.rootNode.child("child").isPresent(), "Root node should contain child node with name 'child'.");
        assertTrue(childNode.hasParents(), "Child node should recognize the root node as its parent.");
    }

    @Test
    @DisplayName("Creating a child node using 'createChildNode' should properly link to the root node")
    void testCreateChildNode() {
        EventNode<Event> childNode = this.rootNode.createChildNode("child");

        assertEquals(1, this.rootNode.children().size(), "Root node should have one child.");
        assertTrue(rootNode.child("child").isPresent(), "Root node should contain child node with name 'child'.");
        assertTrue(((EventNodeImpl<?>) childNode).hasParents(), "Child node should recognize the root node as its parent.");
    }

    @Test
    @DisplayName("Creating a child node with a filter should properly link to the root node")
    void testCreateChildNodeWithFilter() {
        EventNode<Event> childNode = this.rootNode.createChildNode("filteredChild", event -> true);

        assertEquals(1, this.rootNode.children().size(), "Root node should have one child.");
        assertTrue(this.rootNode.child("filteredChild").isPresent(), "Root node should contain child node with name 'filteredChild'.");
        assertTrue(((EventNodeImpl<?>) childNode).hasParents(), "Filtered child node should recognize the root node as its parent.");
    }

    @Test
    @DisplayName("Removing a child node should update the root's children list and child’s parent count")
    void testRemoveChildNode() {
        EventNodeImpl<Event> childNode = new EventNodeImpl<>("child", Event.class, null);
        rootNode.addChildNode(childNode);
        assertEquals(1, rootNode.children().size(), "Root node should have one child after adding.");

        rootNode.removeChildNode("child");

        assertTrue(rootNode.children().isEmpty(), "Root node should have no children after removal.");
        assertFalse(childNode.hasParents(), "Removed child node should have no parents.");
    }
}
