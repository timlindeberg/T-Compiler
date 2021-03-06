Test()

Def Test() =
	var i: Int = 0
	var j: Int = 0
	var k: Float = 0.0f
	var l: Float = 0.0f
	var b: Bool = true
	var h: H = new H()
	var arr: Int[] = new Int[3]

	// Local variable int
	j = i++
	println(i) // res: 1
	println(j) // res: 0
	j = i--
	println(i) // res: 0
	println(j) // res: 1
	j = --i
	println(i) // res: -1
	println(j) // res: -1
	j = ++i
	println(i) // res: 0
	println(j) // res: 0
	i++
	println(i) // res: 1

	// Local variable float
	l = k++
	println(k) // res: 1.0
	println(l) // res: 0.0
	l = k--
	println(k) // res: 0.0
	println(l) // res: 1.0
	l = --k
	println(k) // res: -1.0
	println(l) // res: -1.0
	l = ++k
	println(k) // res: 0.0
	println(l) // res: 0.0
	k++
	println(k) // res: 1.0

	// Static field int
	h.J = H.I++
	println(H.I) // res: 1
	println(H.J) // res: 0
	H.J = h.I--
	println(H.I) // res: 0
	println(h.J) // res: 1
	H.J = --h.I
	println(H.I) // res: -1
	println(H.J) // res: -1
	H.J = ++h.I
	println(H.I) // res: 0
	println(H.J) // res: 0
	h.I++
	println(H.I) // res: 1

	// Field int
	h.L = h.K++
	println(h.K) // res: 1
	println(h.L) // res: 0
	h.L = h.K--
	println(h.K) // res: 0
	println(h.L) // res: 1
	h.L = --h.K
	println(h.K) // res: -1
	println(h.L) // res: -1
	h.L = ++h.K
	println(h.K) // res: 0
	println(h.L) // res: 0
	h.K++
	println(h.K) // res: 1

	// Array int
	i = arr[1]++
	println(arr[1]) // res: 1
	println(i) // res: 0
	i = arr[1]--
	println(arr[1]) // res: 0
	println(i) // res: 1
	i = --arr[1]
	println(arr[1]) // res: -1
	println(i) // res: -1
	i = ++arr[1]
	println(arr[1]) // res: 0
	println(i) // res: 0
	arr[1]++
	println(arr[1]) // res: 1

	H.I = 0
	H.J = 0

	// More advanced accesses
	GetNewH(1 + 5).J = GetNewH(6 *7 + 5).I++
	println(h.I) // res: 1
	println(h.J) // res: 0
	GetNewH(1 + 5).J = GetNewH(6 *7 + 5).I--
	println(h.I) // res: 0
	println(h.J) // res: 1
	GetNewH(1 + 5).J = --GetNewH(6 *7 + 5).I
	println(h.I) // res: -1
	println(h.J) // res: -1
	GetNewH(1 + 5).J = ++GetNewH(6 *7 + 5).I
	println(h.I) // res: 0
	println(h.J) // res: 0
	GetNewH(6 *7 + 5).I++
	println(h.I) // res: 1

	val a = Arr.GetIntArr(5 - 4)

	// More advanced array reads
	i = Arr.GetIntArr(5 - 4)[5 - 4]++
	println(a[1]) // res: 1
	println(i) // res: 0
	i = Arr.GetIntArr(5 - 4)[5 - 4]--
	println(a[1]) // res: 0
	println(i) // res: 1
	i = --Arr.GetIntArr(5 - 4)[5 - 4]
	println(a[1]) // res: -1
	println(i) // res: -1
	i = ++Arr.GetIntArr(5 - 4)[5 - 4]
	println(a[1]) // res: 0
	println(i) // res: 0
	a[5 - 4]++
	println(a[1]) // res: 1

Def GetNewH(i: Int) = i < 5 ? new H() : new H()


class Arr =

	val static a = new Int[3]
	Def static GetIntArr(i: Int) = a

class H =

	Var static I: Int = 0
	Var static J: Int = 0

	Var K: Int = 0
	Var L: Int = 0
