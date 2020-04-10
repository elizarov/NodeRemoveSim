class Node(val i: Int) {
    var next: Node? = null
    var prev: Node? = null
    var removed = false

    override fun toString(): String = "N$i(" +
        "next=${next.id()}," +
        "prev=${prev.id()}," +
        "${if (removed) "R" else "A"})"
}

fun Node?.id() = this?.i?.toString() ?: "_"

fun Node.rightmostAliveNode(): Node {
    var cur = next!!
    while (cur.removed) cur = cur.next!!
    return cur
}

fun Node.leftmostAliveNode(): Node {
    var cur = prev!!
    while (cur.removed) cur = cur.prev!!
    return cur
}

class Process(val node: Node) {
    var step = 0
    var prev: Node? = null // leftmost alive
    var next: Node? = null // rightmost alive

    val isFinal: Boolean
        get() = step == -1
}

data class Parent(val from: State, val processIndex: Int)

class State(val nNodes: Int, removeNodes: List<Int>, val parent: Parent? = null) {
    val nodes = Array(nNodes) { i -> Node(i) }
    val processes = removeNodes.map { Process(nodes[it]) }.toTypedArray()
    val nProcesses = processes.size

    init {
        for (i in 0 until nNodes) {
            nodes[i].next = nodes.getOrNull(i + 1)
            nodes[i].prev = nodes.getOrNull(i - 1)
        }
    }

    val isFinal: Boolean
        get() = processes.all { it.isFinal }

    fun copy(parent: Parent): State {
        val new = State(nNodes, processes.map { it.node.i }, parent)
        for (i in 0 until nNodes) {
            new.nodes[i].next = nodes[i].next?.i?.let { new.nodes[it] }
            new.nodes[i].prev = nodes[i].prev?.i?.let { new.nodes[it] }
            new.nodes[i].removed = nodes[i].removed
        }
        for (j in 0 until nProcesses) {
            new.processes[j].step = processes[j].step
            new.processes[j].prev = processes[j].prev?.i?.let { new.nodes[it] }
            new.processes[j].next = processes[j].next?.i?.let { new.nodes[it] }
        }
        return new
    }

    fun signature(): List<Int> = nodes.flatMap { node ->
        listOf(
            node.next?.i ?: -1,
            node.prev?.i ?: -1,
            if (node.removed) 1 else 0
        )
    } + processes.flatMap { proc ->
        listOf(
            proc.step,
            proc.next?.i ?: -1,
            proc.prev?.i ?: -1
        )
    }

    override fun equals(other: Any?): Boolean = other is State && signature().equals(other.signature())
    override fun hashCode(): Int = signature().hashCode()

    fun validate(): String? {
        val alive = nodes.filter { !it.removed }
        val aliveParis = alive.zipWithNext()
        for ((prev, next) in aliveParis) {
            if (prev.next !== next) return "Wrong next in $prev, expected $next"
            if (next.prev !== prev) return "Wrong prev in $next, expected $prev"
        }
        val first = alive.first()
        if (first.prev != null) return "Wrong prev in $first, expected null"
        val last = alive.last()
        if (last.next != null) return "Wrong next in $last, expected null"
        return null
    }

    fun Process.executeStep() = when (step) {
        0 -> {
            node.removed = true
            step = 1
        }
        1 -> {
            prev = node.leftmostAliveNode()
            step = 2
        }
        2 -> {
            next = node.rightmostAliveNode()
            step = 3
        }
        3 -> {
            next!!.prev = prev
            step = 4
        }
        4 -> {
            prev!!.next = next
            step = 5
        }
        5 -> {
            step = if (next!!.removed) 1 else 6
        }
        6 -> {
            step = if (prev!!.removed) 1 else -1
        }
        -1 -> { /* done, nothing more to do */
        }
        else -> error("Invalid step=$step")
    }

    fun Process.explainStep(): String = when (step) {
        0 -> "node.removed = true // node=${node.id()}"
        1 -> "prev = node.leftmostAliveNode // node=${node.id()}, prev<-${node.leftmostAliveNode().id()}"
        2 -> "next = node.rightmostAliveNode // node=${node.id()}, next<-${node.rightmostAliveNode().id()}"
        3 -> "next.prev = prev // next=${next.id()}, prev=N${prev.id()}"
        4 -> "prev.next = next // next=${next.id()}, prev=N${prev.id()}"
        5 -> "if (next.removed) continue // next=${next.id()}, removed=${next?.removed}"
        6 -> "if (prev.removed) continue // prev=${prev.id()}, removed=${prev?.removed}"
        -1 -> "return"
        else -> error("Invalid step=$step")
    }

    override fun toString(): String = nodes.joinToString()
}

tailrec fun State.explainExecution(to: ArrayList<String> = ArrayList()): List<String> {
    if (parent == null) {
        to.reverse()
        return to
    }
    val (from, j) = parent
    val proc = from.processes[j]
    to += "$from -> P${proc.node.i} @${proc.step}: ${proc.explainStep()}"
    return from.explainExecution(to)
}


fun main() {
    val initial = State(4, listOf(1, 2))
    initial.validate()?.let { error(it) }
    val queue = ArrayDeque<State>()
    val seen = HashSet<State>()
    val final = ArrayList<State>()
    fun enqueue(state: State) {
        if (seen.add(state)) {
            if (state.isFinal)
                final.add(state)
            else
                queue.add(state)
        }
    }
    enqueue(initial)
    while (!queue.isEmpty()) {
        val state0 = queue.removeFirst()
        for (j in 0 until state0.nProcesses) {
            if (state0.processes[j].isFinal) continue
            val state = state0.copy(Parent(state0, j))
            with(state) { processes[j].executeStep() }
            enqueue(state)
        }
    }
    println("Processed ${seen.size} states, ${final.size} final states found")
    for (state in final) {
        state.validate()?.let {
            println("------ Found invalid final state $state")
            println("Invalid: $it")
            println("An example execution that produces this state:")
            state.explainExecution().forEach { println("  $it") }
        }
    }
}