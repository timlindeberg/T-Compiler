// Ignore
import T::std::Vector
import T::std::HashMap
import java::util::regex::Matcher
import java::util::regex::Pattern
import java::lang::Math

val input = `Step Q must be finished before step O can begin.
Step Z must be finished before step G can begin.
Step W must be finished before step V can begin.
Step C must be finished before step X can begin.
Step O must be finished before step E can begin.
Step K must be finished before step N can begin.
Step P must be finished before step I can begin.
Step X must be finished before step D can begin.
Step N must be finished before step E can begin.
Step F must be finished before step A can begin.
Step U must be finished before step Y can begin.
Step M must be finished before step H can begin.
Step J must be finished before step B can begin.
Step B must be finished before step E can begin.
Step S must be finished before step L can begin.
Step A must be finished before step L can begin.
Step E must be finished before step L can begin.
Step L must be finished before step G can begin.
Step D must be finished before step I can begin.
Step Y must be finished before step I can begin.
Step I must be finished before step G can begin.
Step G must be finished before step R can begin.
Step V must be finished before step T can begin.
Step R must be finished before step H can begin.
Step H must be finished before step T can begin.
Step S must be finished before step E can begin.
Step C must be finished before step E can begin.
Step P must be finished before step T can begin.
Step I must be finished before step H can begin.
Step O must be finished before step P can begin.
Step M must be finished before step L can begin.
Step S must be finished before step D can begin.
Step P must be finished before step D can begin.
Step P must be finished before step R can begin.
Step I must be finished before step R can begin.
Step Y must be finished before step G can begin.
Step Q must be finished before step L can begin.
Step N must be finished before step R can begin.
Step J must be finished before step E can begin.
Step N must be finished before step T can begin.
Step B must be finished before step V can begin.
Step Q must be finished before step B can begin.
Step J must be finished before step H can begin.
Step F must be finished before step B can begin.
Step W must be finished before step X can begin.
Step S must be finished before step T can begin.
Step J must be finished before step G can begin.
Step O must be finished before step R can begin.
Step K must be finished before step B can begin.
Step Z must be finished before step O can begin.
Step Q must be finished before step S can begin.
Step K must be finished before step V can begin.
Step B must be finished before step R can begin.
Step J must be finished before step T can begin.
Step E must be finished before step T can begin.
Step G must be finished before step V can begin.
Step D must be finished before step Y can begin.
Step M must be finished before step Y can begin.
Step F must be finished before step G can begin.
Step C must be finished before step P can begin.
Step V must be finished before step R can begin.
Step R must be finished before step T can begin.
Step J must be finished before step Y can begin.
Step U must be finished before step R can begin.
Step Z must be finished before step F can begin.
Step Q must be finished before step V can begin.
Step U must be finished before step M can begin.
Step J must be finished before step R can begin.
Step L must be finished before step V can begin.
Step W must be finished before step K can begin.
Step B must be finished before step Y can begin.
Step O must be finished before step N can begin.
Step D must be finished before step V can begin.
Step P must be finished before step B can begin.
Step U must be finished before step I can begin.
Step O must be finished before step T can begin.
Step S must be finished before step G can begin.
Step X must be finished before step A can begin.
Step U must be finished before step T can begin.
Step A must be finished before step I can begin.
Step B must be finished before step G can begin.
Step N must be finished before step Y can begin.
Step Z must be finished before step J can begin.
Step M must be finished before step D can begin.
Step U must be finished before step A can begin.
Step S must be finished before step R can begin.
Step Z must be finished before step A can begin.
Step Y must be finished before step R can begin.
Step E must be finished before step Y can begin.
Step N must be finished before step G can begin.
Step Z must be finished before step X can begin.
Step P must be finished before step X can begin.
Step Z must be finished before step T can begin.
Step Z must be finished before step P can begin.
Step V must be finished before step H can begin.
Step P must be finished before step L can begin.
Step L must be finished before step H can begin.
Step X must be finished before step V can begin.
Step W must be finished before step G can begin.
Step N must be finished before step D can begin.
Step Z must be finished before step U can begin.`

val testInput = `Step C must be finished before step A can begin.
Step C must be finished before step F can begin.
Step A must be finished before step B can begin.
Step A must be finished before step D can begin.
Step B must be finished before step E can begin.
Step D must be finished before step E can begin.
Step F must be finished before step E can begin.
`


class Day7 =

	val graph = new HashMap<Char, Vector<Char>>()
	val parents = new HashMap<Char, Vector<Char>>()
	val visited = new Bool[1 + 'Z' - 'A']
	val regex = Pattern.compile(`Step ([A-Z]) must be finished before step ([A-Z]) can begin.`)

	Def new(input: String) = ParseGraph(input)


	Def Run() =
		val starts = FindStarts()
		val queue = new Vector<Char>()

		for(var i = starts.Size() - 1; i >= 0; i--)
			queue.Add(starts[i])
		println("Starts:" + starts)
		val order = new Vector<Char>()
		while(!queue.IsEmpty())
			val node = queue.Pop()
			if(Visited(node)) continue

			visited[node - 'A'] = true
			print(node)
			order.Add(node)
			val edges = graph[node]
			for(var i = edges.Size() - 1; i >= 0; i--)
				val e = edges[i]
				if(VisitedParents(e))
					queue.Add(e)

	Def Visited(node: Char) = visited[node - 'A']

	Def VisitedParents(node: Char) =
		for(val p in parents[node])
			if(!Visited(p))
				return false
		true

	Def FindStarts() =
		val starts = new Vector<Char>()
		for(val e in parents)
			if(e.Value().IsEmpty())
				starts.Add(e.Key())
		starts.Sort()
		starts

	Def ParseGraph(input: String) =
		for(val line in input.Lines())
			val m = regex.matcher(line)
			m.matches()
			val from = m.group(1)[0]
			val to = m.group(2)[0]

			val n = graph.GetOrDefault(from)
			n.Add(to)
			graph.GetOrDefault(to)

			val p = parents.GetOrDefault(to)
			p.Add(from)
			parents.GetOrDefault(from)

		for(val entry in graph)
			entry.Value().Sort()

		println(graph)
		println(parents)

new Day7(input).Run()
// res: 3006