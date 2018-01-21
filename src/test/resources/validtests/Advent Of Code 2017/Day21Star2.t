// Ignore
// This solution is too slow to be part of the test suite but it's exactly
// the same as Star 1 except more iterations so it doesnt matter
import java::util::Arrays

val input = `../.. => .##/##./.#.
#./.. => .#./#.#/##.
##/.. => #.#/#.#/###
.#/#. => #../.#./.#.
##/#. => ##./#.#/..#
##/## => #.#/#.#/...
.../.../... => ..##/##../##../#.#.
#../.../... => ##.#/..#./#.#./.#..
.#./.../... => ..#./##.#/#.##/###.
##./.../... => ###./##.#/.###/#.#.
#.#/.../... => ##../#..#/.###/#.#.
###/.../... => ...#/#..#/...#/...#
.#./#../... => ...#/.##./#.##/..#.
##./#../... => .##./.#../.##./.#..
..#/#../... => ####/.#../#.#./.###
#.#/#../... => ###./.#../##../....
.##/#../... => ##../#.#./#.#./##..
###/#../... => #.##/#..#/.#../##..
.../.#./... => .#.#/.###/.##./##..
#../.#./... => .###/.##./..##/..##
.#./.#./... => .##./.#.#/#.##/.###
##./.#./... => ..#./..../..#./###.
#.#/.#./... => ..../..#./..##/##..
###/.#./... => .#.#/#..#/.###/#..#
.#./##./... => ..../..#./.#../####
##./##./... => ..##/#.##/..#./#.##
..#/##./... => ..../#.##/.##./####
#.#/##./... => ..##/#.#./.#../.##.
.##/##./... => #.../...#/###./....
###/##./... => .#../#.#./#.##/....
.../#.#/... => #.#./####/#.../..#.
#../#.#/... => ...#/.#.#/###./.#.#
.#./#.#/... => #..#/#.../###./#.##
##./#.#/... => .##./#.../...#/#.##
#.#/#.#/... => #..#/##../##../.#..
###/#.#/... => #.#./...#/.#.#/.##.
.../###/... => .#.#/.##./..#./.#..
#../###/... => .###/..##/#.##/.#..
.#./###/... => #.../#.../.#../#...
##./###/... => .###/...#/.#.#/.#..
#.#/###/... => .#../..##/#..#/#...
###/###/... => .###/##../##.#/#.#.
..#/.../#.. => ##.#/..../...#/..##
#.#/.../#.. => .#.#/###./...#/.#.#
.##/.../#.. => ##.#/.#../####/#.##
###/.../#.. => #.../#..#/###./....
.##/#../#.. => #..#/..#./####/...#
###/#../#.. => ####/###./##.#/....
..#/.#./#.. => .##./.##./##../#..#
#.#/.#./#.. => #..#/#..#/#.../.#..
.##/.#./#.. => ##../##.#/#.##/..##
###/.#./#.. => #.##/..##/.##./#.#.
.##/##./#.. => #.##/..../##../....
###/##./#.. => ###./.#.#/.###/.#..
#../..#/#.. => .###/#.##/..#./.##.
.#./..#/#.. => #..#/..##/.#.#/##..
##./..#/#.. => ###./#.../..##/##..
#.#/..#/#.. => #.../.##./.###/###.
.##/..#/#.. => ...#/##.#/..#./...#
###/..#/#.. => ###./..#./.#../...#
#../#.#/#.. => #..#/...#/..#./.#.#
.#./#.#/#.. => #..#/##.#/####/.##.
##./#.#/#.. => .###/##../..../.#..
..#/#.#/#.. => ..#./##.#/####/###.
#.#/#.#/#.. => #.#./#.##/##.#/.###
.##/#.#/#.. => ..#./####/##../.###
###/#.#/#.. => .#.#/###./.#.#/#...
#../.##/#.. => .###/..##/.#.#/..#.
.#./.##/#.. => #.##/.#../.###/#.#.
##./.##/#.. => .###/#.../#.../..#.
#.#/.##/#.. => ##../...#/..#./...#
.##/.##/#.. => ..##/.#.#/...#/####
###/.##/#.. => ##../.###/##../###.
#../###/#.. => ###./#..#/#.#./....
.#./###/#.. => ..../#.#./.###/.###
##./###/#.. => .###/##../#..#/####
..#/###/#.. => ..../#.#./#..#/##..
#.#/###/#.. => .#.#/..##/##.#/#..#
.##/###/#.. => .#../...#/##../.#..
###/###/#.. => #.../.###/###./##.#
.#./#.#/.#. => .#.#/#.##/###./#...
##./#.#/.#. => .#../.#../.#../.#..
#.#/#.#/.#. => ##.#/..../###./.#..
###/#.#/.#. => #.#./##.#/.#.#/##..
.#./###/.#. => ##.#/..#./..#./#.#.
##./###/.#. => ####/.###/.#.#/.##.
#.#/###/.#. => .#../.###/##../#.#.
###/###/.#. => #.../.##./..##/####
#.#/..#/##. => ..../..#./##../...#
###/..#/##. => .###/..#./#.##/###.
.##/#.#/##. => .###/..../#.#./...#
###/#.#/##. => ###./...#/.###/####
#.#/.##/##. => #.##/#.../..../...#
###/.##/##. => #.../#.../#..#/...#
.##/###/##. => .#../###./.###/..#.
###/###/##. => ##.#/.#../###./.#..
#.#/.../#.# => #.#./#.#./..../...#
###/.../#.# => ####/###./..../##.#
###/#../#.# => .###/##.#/#.##/..#.
#.#/.#./#.# => ###./.###/#.##/....
###/.#./#.# => .##./###./#.#./##..
###/##./#.# => #.../.#.#/#.##/#..#
#.#/#.#/#.# => ..#./#.#./##../..##
###/#.#/#.# => ..#./.#../...#/.##.
#.#/###/#.# => ..#./###./##.#/####
###/###/#.# => #.../#.#./#..#/.#.#
###/#.#/### => ..##/.##./.#.#/#...
###/###/### => .##./..##/####/###.`

val _testInput = `../.# => ##./#../...
.#./..#/### => #..#/..../..../#..#`

class Rule =

	var matches: Char[][][]
	Var Result: Char[][]
	var size = 0

	Def new(s: String) =
		val x = s.Split(" => ")
		val match: String[] = x[0].Split("/")
		val res = x[1].Split("/")
		size = match[0].Size()
		Result = new Char[size + 1][size + 1]
		for(var i = 0; i < size + 1; i++)
			for(var j = 0; j < size + 1; j++)
				Result[i][j] = res[i][j]

		matches = new Char[8][0][0]

		val matchCharArray = new Char[size][size]
		for(var i = 0; i < size; i++)
			for(var j = 0; j < size; j++)
				matchCharArray[i][j] = match[i][j]
		matches[0] = matchCharArray
		matches[1] = Rotated(matches[0])
		matches[2] = Rotated(matches[1])
		matches[3] = Rotated(matches[2])
		matches[4] = Flip(matches[0])
		matches[5] = Flip(matches[1])
		matches[6] = Flip(matches[2])
		matches[7] = Flip(matches[3])

	Def Flip(match: Char[][]) =
		val size = match.Size()
		val transformed = new Char[size][size]
		for(var i = 0; i < size; i++)
			for(var j = 0; j < size; j++)
				transformed[i][j] = match[i][size - j - 1]
		transformed

	Def Rotated(match: Char[][]) =
		val size = match.Size()
		val transformed = new Char[size][size]
		for(var i = 0; i < size; i++)
			for(var j = 0; j < size; j++)
				transformed[i][j] = match[size - j - 1][i]
		transformed


	Def Matches(square: Char[][]) =
		if(square.Size() != size)
			return false
		for(val m in matches)
			if(Arrays.deepEquals(square, m))
				return true
		return false

Def Transform(square: Char[][], rules: Rule[]) =
	for(val rule in rules)
		if(rule.Matches(square))
			return rule.Result
	return null

Def GetSquare(pattern: Char[][], i: Int, j: Int, width: Int) =
	var square = new Char[width][width]
	for(var x = 0; x < width; x++)
		for(var y = 0; y < width; y++)
			square[x][y] = pattern[(width * i) + x][(width * j) + y]
	return square

Def PrintPattern(pattern: Char[][]) =
	val size = pattern.Size()
	println("-------")
	for(var i = 0; i < size; i++)
		for(var j = 0; j < size; j++)
			print(pattern[i][j])
		println()


/* ---------------------------------------------------------------------- */


val lines = input.Split("\r?\n")
val rules = new Rule[lines.Size()]

for(var i = 0; i < lines.Size(); i++)
	rules[i] = new Rule(lines[i])

var pattern = [
    ['.', '#', '.'],
    ['.', '.', '#'],
    ['#', '#', '#'],
]

for(var iteration = 0; iteration < 18; iteration++)
	val size = pattern.Size()
	val width = size % 2 == 0 ? 2 : 3
	val numSquares = size / width
	val newSize = size * (width + 1) / width
	var newPattern = new Char[newSize][newSize]

	for(var i = 0; i < numSquares; i++)
		for(var j = 0; j < numSquares; j++)
			var square = GetSquare(pattern, i, j, width)
			var newSquare = Transform(square, rules)
			for(var x = 0; x < newSquare.Size(); x++)
				for(var y = 0; y < newSquare.Size(); y++)
					newPattern[(i * width) + i + x][(j * width) + j + y] = newSquare[x][y]

	pattern = newPattern

var count = 0
for(var i = 0; i < pattern.Size(); i++)
	for(var j = 0; j < pattern.Size(); j++)
		if(pattern[i][j] == '#')
			count++

println(count) // res: 2758764
