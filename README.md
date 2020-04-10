# Node removal simulator

Simulates the following concurrent algorithm to remove a node from a doubly-linked list and verifies that
at the end no removed nodes are still physically in the list.

```kotlin
node.removed = true                       // @0
while (true) {
    // Read `next` and `prev` pointers ignoring logically removed nodes.
    val prev = node.leftmostAliveNode     // @1 
    val next = node.rightmostAliveNode    // @2 
    // Link `next` and `prev`.
    next.prev = prev                      // @3
    prev.next = next                      // @4
    // Check that prev and next are still alive.
    if (next.removed) continue            // @5
    if (prev.removed) continue            // @6
    // This node is removed.
    return                                // the final state
}
``` 

The simulation is run for 4 nodes `N0`, `N1`, `N2`, `N3` and two processes `P1` and `P2` that
are trying to concurrently remove two middle neighbouring nodes `N1` and `N2`.
It validates that the above algorithm does not leak memory in this scenario.
 
