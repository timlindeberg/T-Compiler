import java::util::Arrays
import T::std::HashMap

val INPUT = [ 11, 11, 13, 7, 0, 15, 5, 5, 4, 4, 1, 1, 7, 1, 15, 11 ]

class Day6 =

	val seen = new HashMap<Int, Int>()
	var memoryBanks: Int[]

	Def new(memoryBanks: Int[]) = this.memoryBanks = memoryBanks

	def redistribute() =
		var maxIndex = 0
		var max = 0
		val N = memoryBanks.Size()
		for(var i = 0; i < N; i++)
			val bank = memoryBanks[i]
			if(bank > max)
				maxIndex = i
				max = bank
		memoryBanks[maxIndex++] = 0
		while(max-- > 0)
			memoryBanks[(maxIndex++) % N]++

	Def Run() =
		var count = 0
		var hash = Arrays.hashCode(memoryBanks)
		while(!seen.Contains(hash))
			seen[hash] = hash
			redistribute()
			hash = Arrays.hashCode(memoryBanks)
			count++
		println("Count: " + count) // res: Count: 4074


new Day6(INPUT).Run()