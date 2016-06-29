package tcompiler
package analyzer

import tcompiler.analyzer.Symbols._
import tcompiler.ast.Trees.Implicit


object Types {

  trait Typed {
    self =>

    private var _tpe: Type = TUntyped

    def setType(tpe: Type): self.type = {
      _tpe = tpe
      this
    }

    def getType: Type = _tpe
  }

  val Primitives = List[Type](
    TInt,
    TLong,
    TFloat,
    TDouble,
    TChar)

  sealed abstract class Type {
    def isSubTypeOf(tpe: Type): Boolean = tpe.isInstanceOf[this.type]
    def isImplicitlyConvertableFrom(tpe: Type): Boolean = {
      if(this == tpe)
        return true

      val implicitTypes = implicitlyConvertableFrom()
      if (implicitTypes.contains(tpe))
        return true


      (this, tpe) match {
        case (TArray(a1), TArray(a2)) => a1.isImplicitlyConvertableFrom(a2)
        case _ => false
      }
    }
    def getSuperTypes: List[Type] = List()
    def isPrimitive = Primitives.contains(this)

    def implicitlyConvertableFrom(): List[Type] = List()


    def byteCodeName: String
    val codes: CodeMap
    val size : Int
  }

  case object TError extends Type {
    override def isSubTypeOf(tpe: Type): Boolean = true
    override def toString = "[error]"
    override def byteCodeName: String = "ERROR"
    override val codes     = EmptyCodeMap
    override val size: Int = 0
  }

  case object TUntyped extends Type {
    override def isSubTypeOf(tpe: Type): Boolean = false
    override def toString = "[untyped]"
    override def byteCodeName: String = "UNTYPED"
    override val codes     = EmptyCodeMap
    override val size: Int = 0
  }

  case object TUnit extends Type {
    override def toString = "Unit"
    override def byteCodeName: String = "V"
    override val codes     = EmptyCodeMap
    override val size: Int = 0
  }

  case object TInt extends Type {
    override def implicitlyConvertableFrom() = List(TChar)
    override def toString = "Int"
    override def byteCodeName: String = "I"
    override val codes     = IntCodeMap
    override val size: Int = 1
  }

  case object TLong extends Type {
    override def implicitlyConvertableFrom() = List(TChar, TInt)
    override def toString = "Long"
    override def byteCodeName: String = "J"
    override val codes     = LongCodeMap
    override val size: Int = 2
  }

  case object TFloat extends Type {
    override def implicitlyConvertableFrom() = List(TLong, TChar, TInt)
    override def toString = "Float"
    override def byteCodeName: String = "F"
    override val codes     = FloatCodeMap
    override val size: Int = 1
  }

  case object TDouble extends Type {
    override def implicitlyConvertableFrom() = List(TFloat, TLong, TChar, TInt)
    override def toString = "Double"
    override def byteCodeName: String = "D"
    override val codes     = DoubleCodeMap
    override val size: Int = 2
  }

  case object TChar extends Type {
    override def toString = "Char"
    override def byteCodeName: String = "C"
    override val codes     = CharCodeMap
    override val size: Int = 1
  }

  case object TBool extends Type {
    override def toString = "Bool"
    override def byteCodeName: String = "Z"
    override val codes     = BoolCodeMap
    override val size: Int = 1
  }

  case class TArray(tpe: Type) extends Type {
    override def isSubTypeOf(otherTpe: Type): Boolean = otherTpe match {
      case TArray(arrTpe) => tpe.isSubTypeOf(arrTpe)
      case _ => false
    }

    override def implicitlyConvertableFrom() = List()
    override def toString = tpe.toString + "[]"
    override def byteCodeName: String = "[" + tpe.byteCodeName
    override val codes     = new ArrayCodeMap(tpe.byteCodeName)
    override val size: Int = 1
    def dimension: Int = tpe match {
      case t: TArray => 1 + t.dimension
      case _ => 1
    }
  }

  case class TObject(classSymbol: ClassSymbol) extends Type {

    override def isSubTypeOf(tpe: Type): Boolean = tpe match {
      case TObject(c) =>
        if (classSymbol.name == c.name || c == Object.classSymbol) true
        else classSymbol.parents exists { parent => parent.getType.isSubTypeOf(tpe) }
      case _ => false
    }

    override def implicitlyConvertableFrom() =
      classSymbol.methods.filter(m =>
        m.name == "new" &&
          m.modifiers.contains(Implicit()) &&
          m.argList.size == 1).
        map(_.argList.head.getType)

    override def getSuperTypes: List[Type] =
      this :: classSymbol.parents.flatMap(_.getType.getSuperTypes)

    override def toString = classSymbol.name
    override def byteCodeName: String = {
      val name = classSymbol.name.replaceAll("\\.", "/")
      s"L$name;"
    }

    def ==(other: TObject): Boolean = classSymbol.name == other.classSymbol.name

    override val codes = new ObjectCodeMap(classSymbol.name)
    override val size = 1
  }

  // For checking of a type is an object
  var Object = TObject(new ClassSymbol("kool/lang/Object", false))
  var String = TObject(new ClassSymbol("kool/lang/String", false))
  val tArray = TArray(Object)
}

