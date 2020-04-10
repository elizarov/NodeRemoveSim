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

The results are:

```text
Processed 166 states, 5 final states found
------ Found invalid final state N0(next=2,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A)
Invalid: Wrong next in N0(next=2,prev=_,A), expected N3(next=_,prev=0,A)
An example execution that produces this state:
  N0(next=1,prev=_,A), N1(next=2,prev=0,A), N2(next=3,prev=1,A), N3(next=_,prev=2,A) -> P1 @0: node.removed = true
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,A), N3(next=_,prev=2,A) -> P1 @1: prev = node.leftmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,A), N3(next=_,prev=2,A) -> P1 @2: next = node.rightmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,A), N3(next=_,prev=2,A) -> P1 @3: next.prev = prev
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,A), N3(next=_,prev=2,A) -> P2 @0: node.removed = true
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=2,A) -> P2 @1: prev = node.leftmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=2,A) -> P2 @2: next = node.rightmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=2,A) -> P2 @3: next.prev = prev
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A) -> P2 @4: prev.next = next
  N0(next=3,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A) -> P1 @4: prev.next = next
  N0(next=2,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A) -> P1 @5: if (next.removed) continue
  N0(next=2,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A) -> P1 @6: if (prev.removed) continue
  N0(next=2,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A) -> P2 @5: if (next.removed) continue
  N0(next=2,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=0,R), N3(next=_,prev=0,A) -> P2 @6: if (prev.removed) continue
------ Found invalid final state N0(next=3,prev=_,A), N1(next=3,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=1,A)
Invalid: Wrong prev in N3(next=_,prev=1,A), expected N0(next=3,prev=_,A)
An example execution that produces this state:
  N0(next=1,prev=_,A), N1(next=2,prev=0,A), N2(next=3,prev=1,A), N3(next=_,prev=2,A) -> P2 @0: node.removed = true
  N0(next=1,prev=_,A), N1(next=2,prev=0,A), N2(next=3,prev=1,R), N3(next=_,prev=2,A) -> P2 @1: prev = node.leftmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,A), N2(next=3,prev=1,R), N3(next=_,prev=2,A) -> P1 @0: node.removed = true
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=2,A) -> P1 @1: prev = node.leftmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=2,A) -> P1 @2: next = node.rightmostAliveNode
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=2,A) -> P1 @3: next.prev = prev
  N0(next=1,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=0,A) -> P1 @4: prev.next = next
  N0(next=3,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=0,A) -> P1 @5: if (next.removed) continue
  N0(next=3,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=0,A) -> P1 @6: if (prev.removed) continue
  N0(next=3,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=0,A) -> P2 @2: next = node.rightmostAliveNode
  N0(next=3,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=0,A) -> P2 @3: next.prev = prev
  N0(next=3,prev=_,A), N1(next=2,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=1,A) -> P2 @4: prev.next = next
  N0(next=3,prev=_,A), N1(next=3,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=1,A) -> P2 @5: if (next.removed) continue
  N0(next=3,prev=_,A), N1(next=3,prev=0,R), N2(next=3,prev=1,R), N3(next=_,prev=1,A) -> P2 @6: if (prev.removed) continue
```