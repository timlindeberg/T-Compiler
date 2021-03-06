import T::std::HashMap
import T::std::Tester
import T::std::MapEntry

IntMap()
StringMap()
ImplicitArrayConstructor()
LargeMap()
InitialCapacity()
ToStringTest()
Get()
Contains()
ContainsValue()
Iterators()
Equals()
Remove()
AddAll()
Foreach()

println("All tests succeeded.") // res: All tests succeeded.

Def IntMap() =
	val t = new Tester<Int>()
	val map = new HashMap<Int, Int>()
	map[1] = 2
	t.AssertEquals(map[1], 2)
	t.AssertEquals(map.Size(), 1)
	map[1] = 3
	t.AssertEquals(map[1], 3)
	t.AssertEquals(map.Size(), 1)
	map[5] = 5
	t.AssertEquals(map[5], 5)
	t.AssertEquals(map.Size(), 2)

Def StringMap() =
	val t = new Tester<Int>()
	val map = new HashMap<String, Int>()
	map["1"] = 2
	t.AssertEquals(map["1"], 2)
	t.AssertEquals(map.Size(), 1)
	map["1"] = 3
	t.AssertEquals(map["1"], 3)
	t.AssertEquals(map.Size(), 1)
	map["5"] = 5
	t.AssertEquals(map["5"], 5)
	t.AssertEquals(map.Size(), 2)

Def ImplicitArrayConstructor() =
	val ti = new Tester<Int>()
	val ts = new Tester<String>()

	val map: HashMap<Int, String> = [
	    [ 1, "1" ],
	    [ 2, "2" ],
	    [ 3, "3" ],
	    [ 4, "4" ]
	]
	ti.AssertEquals(map.Size(), 4)
	ts.AssertEquals(map[1], "1")
	ts.AssertEquals(map[2], "2")
	ts.AssertEquals(map[3], "3")
	ts.AssertEquals(map[4], "4")

	// TODO: This doesn't work since the arrays are inferred as Int[][] and not Object[][]
/*
	val map2: HashMap<Int, Int> = [
		[1, 1],
		[2, 2],
		[3, 3],
		[4, 4] ]
	ti.AssertEquals(map2.Size(), 4)
	ts.AssertEquals(map2[1], 1)
	ts.AssertEquals(map2[2], 2)
	ts.AssertEquals(map2[3], 3)
	ts.AssertEquals(map2[4], 4)
*/

Def LargeMap() =
	val t = new Tester<Int>()
	val map = new HashMap<Int, Int>()
	val count = 500000
	for(var i = 0; i < count; i++)
		map[i] = i + 1
		t.AssertEquals(map.Size(), i + 1)

	for(var i = 0; i < count; i++)
		t.AssertEquals(map[i], i + 1)

	t.AssertEquals(map.Size(), count)

Def InitialCapacity() =
	testCapacity(1, 1)
	testCapacity(2, 2)
	testCapacity(3, 4)
	testCapacity(512, 512)
	testCapacity(513, 1024)

Def ToStringTest() =
	val t = new Tester<String>()
	val map = new HashMap<Int, Int>()
	map[1] = 5
	map[2] = 2
	map[64] = -1
	t.AssertEquals(map.toString(), "[ (64 -> -1), (1 -> 5), (2 -> 2) ]")

Def Get() =
	val to = new Tester<Int>()
	val map = new HashMap<Int, Int>()
	map[1] = 5
	map[2] = 4
	to.AssertEquals(map.Get(1), 5)
	to.AssertEquals(map.Get(2), 4)
	to.AssertEquals(map.Get(3), null)

Def Contains() =
	val t = new Tester<Bool>()
	val map = new HashMap<Int, Int>()
	map[1] = 5
	map[2] = 4
	t.Assert(map.Contains(1))
	t.Assert(map.Contains(2))
	t.AssertFalse(map.Contains(3))

Def ContainsValue() =
	val t = new Tester<Bool>()
	val map = new HashMap<Int, Int>()
	map[1] = 5
	map[2] = 4
	t.Assert(map.ContainsValue(5))
	t.Assert(map.ContainsValue(4))
	t.AssertFalse(map.ContainsValue(1))

Def Iterators() =
	val t = new Tester<Int>()
	val map = new HashMap<Int, Int>()
	val entries = new Int[4][2]
	entries[0] = [ 1, 1 ]
	entries[1] = [ 2, 6 ]
	entries[2] = [ 90, -5 ]
	entries[3] = [-2, 35]

	for(var i = 0; i < entries.Size(); i++)
		map[entries[i][0]] = entries[i][1]
	;
		val it = map.Iterator()
		while(it.HasNext())
			t.Assert(containsEntry(it.Next(), entries))
	;
		val it = map.Keys()
		while(it.HasNext())
			t.Assert(containsKey(it.Next(), entries))
	;
		val it = map.Values()
		while(it.HasNext())
			t.Assert(containsValue(it.Next(), entries))

Def Equals() =
	val t = new Tester<Int>()

	val map1 = new HashMap<Int, Int>()
	val map2 = new HashMap<Int, Int>()
	val map3 = new HashMap<Int, Int>()
	val map4 = new HashMap<Int, Int>()
	val map5 = new HashMap<Int, Int>()

	map1[1] = 5
	map1[2] = 6
	map1[3] = 7
	map1[4] = 8

	map2[1] = 5
	map2[2] = 6
	map2[3] = 7
	map2[4] = 8

	map3[0] = 5
	map3[2] = 6
	map3[3] = 7
	map3[4] = 8

	map4[1] = 5
	map4[2] = 6
	map4[3] = 8
	map4[4] = 8

	map5[1] = 5
	map5[2] = 6
	map5[3] = 7

	t.Assert(map1 == map2)
	t.Assert(map2 == map1)

	t.Assert(map1 != map3)
	t.Assert(map1 != map4)
	t.Assert(map1 != map5)

	t.Assert(map2 != map3)
	t.Assert(map2 != map4)
	t.Assert(map2 != map5)

	t.Assert(map3 != map1)
	t.Assert(map3 != map2)
	t.Assert(map3 != map4)
	t.Assert(map3 != map5)

	t.Assert(map4 != map1)
	t.Assert(map4 != map2)
	t.Assert(map4 != map3)
	t.Assert(map4 != map5)

	t.Assert(map5 != map1)
	t.Assert(map5 != map2)
	t.Assert(map5 != map3)
	t.Assert(map5 != map4)

Def Remove() =
	val t = new Tester<Int>()
	val map = new HashMap<Int, Int>()

	map[5] = 10
	map[4] = 11
	map[3] = 12

	t.AssertEquals(map.Size(), 3)
	t.Assert(map.Contains(5))
	t.Assert(map.ContainsValue(10))
	t.Assert(map.Contains(4))
	t.Assert(map.ContainsValue(11))
	t.Assert(map.Contains(3))
	t.Assert(map.ContainsValue(12))

	map.Remove(5)

	t.AssertEquals(map.Size(), 2)
	t.AssertFalse(map.Contains(5))
	t.AssertFalse(map.ContainsValue(10))
	t.Assert(map.Contains(4))
	t.Assert(map.ContainsValue(11))
	t.Assert(map.Contains(3))
	t.Assert(map.ContainsValue(12))

	map.Remove(4)

	t.AssertEquals(map.Size(), 1)
	t.AssertFalse(map.Contains(5))
	t.AssertFalse(map.ContainsValue(10))
	t.AssertFalse(map.Contains(4))
	t.AssertFalse(map.ContainsValue(11))
	t.Assert(map.Contains(3))
	t.Assert(map.ContainsValue(12))

	map.Remove(3)

	t.AssertEquals(map.Size(), 0)
	t.AssertFalse(map.Contains(5))
	t.AssertFalse(map.ContainsValue(10))
	t.AssertFalse(map.Contains(4))
	t.AssertFalse(map.ContainsValue(11))
	t.AssertFalse(map.Contains(3))
	t.AssertFalse(map.ContainsValue(12))

	map.Remove(2)

	t.AssertEquals(map.Size(), 0)
	t.AssertFalse(map.Contains(5))
	t.AssertFalse(map.ContainsValue(10))
	t.AssertFalse(map.Contains(4))
	t.AssertFalse(map.ContainsValue(11))
	t.AssertFalse(map.Contains(3))
	t.AssertFalse(map.ContainsValue(12))

Def AddAll() =
	val t = new Tester<Int>()
	val map1 = new HashMap<Int, Int>()
	val map2 = new HashMap<Int, Int>()

	map1[1] = 2
	map1[2] = 3

	map2[2] = 3
	map2[3] = 4
	map2[4] = 5

	map1.AddAll(map2)
	t.AssertEquals(map1.Size(), 4)
	t.Assert(map1.Contains(1))
	t.Assert(map1.Contains(2))
	t.Assert(map1.Contains(3))
	t.Assert(map1.Contains(4))

	t.Assert(map1.ContainsValue(2))
	t.Assert(map1.ContainsValue(3))
	t.Assert(map1.ContainsValue(4))
	t.Assert(map1.ContainsValue(5))

	map2.AddAll(map1)

	t.Assert(map1 == map2)

Def Foreach() =
	val t = new Tester<Int>()
	val map: HashMap<Int, Int> = new HashMap<Int, Int>()

	map[1] = 2
	map[2] = 3
	map[3] = 4
	map[4] = 5

	var i = 1
	for(val kv in map)
		t.AssertEquals(kv.Key(), i)
		t.AssertEquals(kv.Value(), i + 1)
		i++

def testCapacity(given: Int, expected: Int) =
	val t = new Tester<Int>()
	val map = new HashMap<Int, Int>(given, 0.75)
	t.AssertEquals(map.Capacity(), expected)

def containsEntry(entry: MapEntry<Int, Int>, entries: Int[][]) =
	for(var i = 0; i < entries.Size(); i++)
		if(entries[i][0] == entry.Key() && entries[i][1] == entry.Value())
			return true
	return false

def containsKey(key: Int, entries: Int[][]) =
	for(var i = 0; i < entries.Size(); i++)
		if(entries[i][0] == key)
			return true
	return false

def containsValue(value: Int, entries: Int[][]) =
	for(var i = 0; i < entries.Size(); i++)
		if(entries[i][1] == value)
			return true
	return false
