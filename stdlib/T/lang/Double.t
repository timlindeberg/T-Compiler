package T::lang

import T::lang::Char
import T::lang::Int
import T::lang::Long
import T::lang::Float
import T::lang::Bool

class Double =

	Def new() = ;
	Def new(v: Double) = ;
	Def implicit new(v: Int) = ;
	Def implicit new(v: Long) = ;
	Def implicit new(v: Float) = ;
	Def implicit new(v: Char) = ;

	// Arithmetic operators

	Def +(a: Double, b: Double) : Double = ;
	Def +(a: Double, b: Float)  : Double = ;
	Def +(a: Float,  b: Double) : Double = ;
	Def +(a: Double, b: Int)    : Double = ;
	Def +(a: Int,    b: Double) : Double = ;
	Def +(a: Double, b: Long)   : Double = ;
	Def +(a: Long,   b: Double) : Double = ;
	Def +(a: Double, b: Char)   : Double = ;
	Def +(a: Char,   b: Double) : Double = ;

	Def -(a: Double, b: Double) : Double = ;
	Def -(a: Double, b: Float)  : Double = ;
	Def -(a: Float,  b: Double) : Double = ;
	Def -(a: Double, b: Int)    : Double = ;
	Def -(a: Int,    b: Double) : Double = ;
	Def -(a: Double, b: Long)   : Double = ;
	Def -(a: Long,   b: Double) : Double = ;
	Def -(a: Double, b: Char)   : Double = ;
	Def -(a: Char,   b: Double) : Double = ;

	Def *(a: Double, b: Double) : Double = ;
	Def *(a: Double, b: Float)  : Double = ;
	Def *(a: Float,  b: Double) : Double = ;
	Def *(a: Double, b: Int)    : Double = ;
	Def *(a: Int,    b: Double) : Double = ;
	Def *(a: Double, b: Long)   : Double = ;
	Def *(a: Long,   b: Double) : Double = ;
	Def *(a: Double, b: Char)   : Double = ;
	Def *(a: Char,   b: Double) : Double = ;

	Def /(a: Double, b: Double) : Double = ;
	Def /(a: Double, b: Float)  : Double = ;
	Def /(a: Float,  b: Double) : Double = ;
	Def /(a: Double, b: Int)    : Double = ;
	Def /(a: Int,    b: Double) : Double = ;
	Def /(a: Double, b: Long)   : Double = ;
	Def /(a: Long,   b: Double) : Double = ;
	Def /(a: Double, b: Char)   : Double = ;
	Def /(a: Char,   b: Double) : Double = ;

	Def %(a: Double, b: Double) : Double = ;
	Def %(a: Double, b: Float)  : Double = ;
	Def %(a: Float,  b: Double) : Double = ;
	Def %(a: Double, b: Int)    : Double = ;
	Def %(a: Int,    b: Double) : Double = ;
	Def %(a: Double, b: Long)   : Double = ;
	Def %(a: Long,   b: Double) : Double = ;
	Def %(a: Double, b: Char)   : Double = ;
	Def %(a: Char,   b: Double) : Double = ;

	// Comparison operators

	Def <(a: Double, b: Double) : Bool = ;
	Def <(a: Double, b: Float)  : Bool = ;
	Def <(a: Float,  b: Double) : Bool = ;
	Def <(a: Double, b: Int)    : Bool = ;
	Def <(a: Int,    b: Double) : Bool = ;
	Def <(a: Double, b: Long)   : Bool = ;
	Def <(a: Long,   b: Double) : Bool = ;
	Def <(a: Double, b: Char)   : Bool = ;
	Def <(a: Char,   b: Double) : Bool = ;

	Def <=(a: Double, b: Double) : Bool = ;
	Def <=(a: Double, b: Float)  : Bool = ;
	Def <=(a: Float,  b: Double) : Bool = ;
	Def <=(a: Double, b: Int)    : Bool = ;
	Def <=(a: Int,    b: Double) : Bool = ;
	Def <=(a: Double, b: Long)   : Bool = ;
	Def <=(a: Long,   b: Double) : Bool = ;
	Def <=(a: Double, b: Char)   : Bool = ;
	Def <=(a: Char,   b: Double) : Bool = ;

	Def >(a: Double, b: Double) : Bool = ;
	Def >(a: Double, b: Float)  : Bool = ;
	Def >(a: Float,  b: Double) : Bool = ;
	Def >(a: Double, b: Int)    : Bool = ;
	Def >(a: Int,    b: Double) : Bool = ;
	Def >(a: Double, b: Long)   : Bool = ;
	Def >(a: Long,   b: Double) : Bool = ;
	Def >(a: Double, b: Char)   : Bool = ;
	Def >(a: Char,   b: Double) : Bool = ;

	Def >=(a: Double, b: Double) : Bool = ;
	Def >=(a: Double, b: Float)  : Bool = ;
	Def >=(a: Float,  b: Double) : Bool = ;
	Def >=(a: Double, b: Int)    : Bool = ;
	Def >=(a: Int,    b: Double) : Bool = ;
	Def >=(a: Double, b: Long)   : Bool = ;
	Def >=(a: Long,   b: Double) : Bool = ;
	Def >=(a: Double, b: Char)   : Bool = ;
	Def >=(a: Char,   b: Double) : Bool = ;

	// Equals operators

	Def ==(a: Double, b: Double) : Bool = ;
	Def ==(a: Double, b: Float)  : Bool = ;
	Def ==(a: Float,  b: Double) : Bool = ;
	Def ==(a: Double, b: Int)    : Bool = ;
	Def ==(a: Int,    b: Double) : Bool = ;
	Def ==(a: Double, b: Long)   : Bool = ;
	Def ==(a: Long,   b: Double) : Bool = ;
	Def ==(a: Double, b: Char)   : Bool = ;
	Def ==(a: Char,   b: Double) : Bool = ;

	Def !=(a: Double, b: Double) : Bool = ;
	Def !=(a: Double, b: Float)  : Bool = ;
	Def !=(a: Float,  b: Double) : Bool = ;
	Def !=(a: Double, b: Int)    : Bool = ;
	Def !=(a: Int,    b: Double) : Bool = ;
	Def !=(a: Double, b: Long)   : Bool = ;
	Def !=(a: Long,   b: Double) : Bool = ;
	Def !=(a: Double, b: Char)   : Bool = ;
	Def !=(a: Char,   b: Double) : Bool = ;

	// Unary operators
	Def #(a: Double) : Int = ;
	Def -(a: Double) : Double = ;
	Def ++(a: Double): Double = ;
	Def --(a: Double): Double = ;
