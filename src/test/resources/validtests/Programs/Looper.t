println(run(100)); // res: 20
println(new Loopy().run(100));  // res: 100
println(new Loopy2().run(100)); // res: 300
println(new Loopy3().run(100)); // res: 0

Def run(t: Int): Int =
	var times = t
	var value: Int;
	value = 0;
	while((true || (1/1 == 1)) && value < times)
		value = value + 1;
		times = times - 1;

	while(value < times*2)
		value = value + 1;

	return value / 5;

class Counter =
	var count: Int;

	Def init(): Counter =
		count = 0;
		return this;

	Def increment(): Int =
		count = count + 1;
		return count;

	Def count(): Int =
		return count;

class Loopy =
	Def run(times: Int): Int =
		var c: Counter;
		c = new Counter();
		c = c.init();
		while (c.count() < times) while (c.count() < times) while (c.increment() < times) ;
		return c.count();

class Loopy2 =
	var c1: Counter;
	var c2: Counter;
	var c3: Counter;
	Def run(times: Int): Int =
		var tmp: Int;
		c1 = new Counter();
		c1 = c1.init();
		c2 = new Counter();
		c2 = c2.init();
		c3 = new Counter();
		c3 = c3.init();
		while (c1.count() < times || c2.count() < times || c3.count() < times)
			if (c1.count() < c2.count())
				tmp = c1.increment();
			else if (c2.count() < c3.count())
				tmp = c2.increment();
			else
				tmp = c3.increment();
		return c1.count() + c2.count() + c3.count();

class Loopy3 =
	Def run(t: Int): Int =
		var times = t
		var l: Loopy3;
		while (!(times < 1))
			l = new Loopy3();
			times = l.run(times-1);

		return times;

