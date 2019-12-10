import T::std::Vector

val input = "1102,34463338,34463338,63,1007,63,34463338,63,1005,63,53,1102,3,1,1000,109,988,209,12,9,1000,209,6,209,3,203,0,1008,1000,1,63,1005,63,65,1008,1000,2,63,1005,63,904,1008,1000,0,63,1005,63,58,4,25,104,0,99,4,0,104,0,99,4,17,104,0,99,0,0,1102,1,38,1003,1102,24,1,1008,1102,1,29,1009,1102,873,1,1026,1102,1,32,1015,1102,1,1,1021,1101,0,852,1023,1102,1,21,1006,1101,35,0,1018,1102,1,22,1019,1102,839,1,1028,1102,1,834,1029,1101,0,36,1012,1101,0,31,1011,1102,23,1,1000,1101,405,0,1024,1101,33,0,1013,1101,870,0,1027,1101,0,26,1005,1101,30,0,1004,1102,1,39,1007,1101,0,28,1017,1101,34,0,1001,1102,37,1,1014,1101,20,0,1002,1102,1,0,1020,1101,0,859,1022,1102,1,27,1016,1101,400,0,1025,1102,1,25,1010,109,-6,1207,10,29,63,1005,63,201,1001,64,1,64,1105,1,203,4,187,1002,64,2,64,109,3,2107,25,8,63,1005,63,221,4,209,1106,0,225,1001,64,1,64,1002,64,2,64,109,-4,2101,0,9,63,1008,63,18,63,1005,63,245,1106,0,251,4,231,1001,64,1,64,1002,64,2,64,109,3,2108,38,7,63,1005,63,273,4,257,1001,64,1,64,1106,0,273,1002,64,2,64,109,22,21102,40,1,0,1008,1018,40,63,1005,63,299,4,279,1001,64,1,64,1106,0,299,1002,64,2,64,109,-16,21108,41,41,10,1005,1012,321,4,305,1001,64,1,64,1105,1,321,1002,64,2,64,109,6,2102,1,-2,63,1008,63,22,63,1005,63,341,1105,1,347,4,327,1001,64,1,64,1002,64,2,64,109,21,1206,-8,359,1106,0,365,4,353,1001,64,1,64,1002,64,2,64,109,-7,21101,42,0,-6,1008,1016,44,63,1005,63,389,1001,64,1,64,1105,1,391,4,371,1002,64,2,64,109,2,2105,1,0,4,397,1106,0,409,1001,64,1,64,1002,64,2,64,109,-3,1205,0,427,4,415,1001,64,1,64,1105,1,427,1002,64,2,64,109,-13,2102,1,-1,63,1008,63,39,63,1005,63,449,4,433,1106,0,453,1001,64,1,64,1002,64,2,64,109,-10,1202,4,1,63,1008,63,20,63,1005,63,479,4,459,1001,64,1,64,1106,0,479,1002,64,2,64,109,7,2108,37,-2,63,1005,63,495,1105,1,501,4,485,1001,64,1,64,1002,64,2,64,109,4,21101,43,0,1,1008,1010,43,63,1005,63,523,4,507,1106,0,527,1001,64,1,64,1002,64,2,64,109,-4,1208,-5,23,63,1005,63,549,4,533,1001,64,1,64,1106,0,549,1002,64,2,64,109,-4,1208,7,27,63,1005,63,565,1106,0,571,4,555,1001,64,1,64,1002,64,2,64,109,15,1205,4,587,1001,64,1,64,1106,0,589,4,577,1002,64,2,64,109,-7,1202,-7,1,63,1008,63,18,63,1005,63,613,1001,64,1,64,1106,0,615,4,595,1002,64,2,64,109,5,21107,44,43,1,1005,1015,635,1001,64,1,64,1105,1,637,4,621,1002,64,2,64,109,-2,21102,45,1,6,1008,1018,44,63,1005,63,661,1001,64,1,64,1105,1,663,4,643,1002,64,2,64,109,-18,1207,6,24,63,1005,63,685,4,669,1001,64,1,64,1105,1,685,1002,64,2,64,109,4,2101,0,8,63,1008,63,21,63,1005,63,707,4,691,1105,1,711,1001,64,1,64,1002,64,2,64,109,17,1206,5,725,4,717,1105,1,729,1001,64,1,64,1002,64,2,64,109,9,21107,46,47,-9,1005,1015,751,4,735,1001,64,1,64,1106,0,751,1002,64,2,64,109,-9,1201,-6,0,63,1008,63,26,63,1005,63,775,1001,64,1,64,1106,0,777,4,757,1002,64,2,64,109,-15,1201,0,0,63,1008,63,23,63,1005,63,803,4,783,1001,64,1,64,1105,1,803,1002,64,2,64,109,-1,2107,30,10,63,1005,63,819,1106,0,825,4,809,1001,64,1,64,1002,64,2,64,109,24,2106,0,5,4,831,1105,1,843,1001,64,1,64,1002,64,2,64,109,-5,2105,1,5,1001,64,1,64,1105,1,861,4,849,1002,64,2,64,109,14,2106,0,-5,1105,1,879,4,867,1001,64,1,64,1002,64,2,64,109,-17,21108,47,44,4,1005,1019,899,1001,64,1,64,1105,1,901,4,885,4,64,99,21101,0,27,1,21102,915,1,0,1106,0,922,21201,1,58969,1,204,1,99,109,3,1207,-2,3,63,1005,63,964,21201,-2,-1,1,21101,0,942,0,1105,1,922,22102,1,1,-1,21201,-2,-3,1,21101,957,0,0,1106,0,922,22201,1,-1,-2,1106,0,968,21201,-2,0,-2,109,-3,2105,1,0"
val testInput = "104,1125899906842624,99"

class Param =
	Var Base: Long
	Var Value: Long
	Var Mode: Int

	var program: Vector<Long>

	Def new(program: Vector<Long>, base: Long, value: Long, mode: Int) =
		this.program = program
		Base = base
		Value = value
		Mode = mode

	Def Get(): Long = Mode == 0 ? program[Value as Int] :
	                  Mode == 1 ? Value :
	                  program[(Base + Value) as Int]

	Def toString() = Value + "(" + Mode + "): " + Get()

class Day5 =

	var program: Vector<Long>
	val input: Long = 1
	var base: Long = 0

	Def Run(input: String) =
		program = new Vector<Long>(input.Size() * 100, 0)

		val split = input.Split(",")
		var i = 0
		for (val x in split)
			program[i++] = x.ToLong()
		Execute()


	Def Execute() =
		var pc = 0

		for (;;)
			val s = program[pc].toString()
			val opCode = s[-2:].ToInt()
			if(opCode == 99)
				break
			val parameterModes = s[:-2]
			if (opCode == 1)
				val params = GetParameters(pc, parameterModes, 3)
				Write(params[2], params[0].Get() + params[1].Get())
				pc += 4
			if (opCode == 2)
				val params = GetParameters(pc, parameterModes, 3)
				Write(params[2], params[0].Get() * params[1].Get())
				pc += 4
			if (opCode == 3)
				val params = GetParameters(pc, parameterModes, 1)
				Write(params[0], input)
				pc += 2
			if (opCode == 4)
				val params = GetParameters(pc, parameterModes, 1)
				println(params[0].Get())
				pc += 2
			if (opCode == 5)
				val params = GetParameters(pc, parameterModes, 2)
				pc = params[0].Get() != 0 ? params[1].Get() as Int : pc + 3
			if (opCode == 6)
				val params = GetParameters(pc, parameterModes, 2)
				pc = params[0].Get() == 0 ? params[1].Get() as Int : pc + 3
			if (opCode == 7)
				val params = GetParameters(pc, parameterModes, 3)
				val v1 = params[0].Get()
				val v2 = params[1].Get()
				Write(params[2], v1 < v2 ? 1 : 0)
				pc += 4
			if (opCode == 8)
				val params = GetParameters(pc, parameterModes, 3)
				val v1 = params[0].Get()
				val v2 = params[1].Get()
				Write(params[2], v1 == v2 ? 1 : 0)
				pc += 4
			if (opCode == 9)
				val params = GetParameters(pc, parameterModes, 1)
				base += params[0].Get()
				pc += 2

	Def Write(destination: Param, value: Long) =
		var dest = destination.Value
		if(destination.Mode == 2)
			dest += destination.Base
		program[dest as Int] = value

	Def GetParameters(pc: Int, parameterModes: String, numParameters: Int) =
		val values = new Long[numParameters]
		for(var i = 0; i < numParameters; i++)
			values[i] = program[pc + i + 1]

		val params = new Vector<Param>()
		val n = parameterModes.Size();
		for(var i = 0 ; i < values.Size(); i++)
			val mode = i < n ? parameterModes[n - i - 1].NumericValue() : 0
			params.Add(new Param(program, base, values[i], mode))

		return params

new Day5().Run(input) // res: 3235019597