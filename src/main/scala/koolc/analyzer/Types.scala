package koolc
package analyzer

import Symbols._
import cafebabe.ByteCodes.IASTORE
import cafebabe.CodeHandler
import koolc.code.CodeGeneration
import cafebabe.AbstractByteCodes._
import cafebabe.ByteCodes._

object Types {
  trait Typed {
    self =>

    private var _tpe: Type = TUntyped

    def setType(tpe: Type): self.type = { _tpe = tpe; this }
    def getType: Type = _tpe
  }

  sealed abstract class Type {
    def isSubTypeOf(tpe: Type): Boolean = tpe.isInstanceOf[this.type]
    def getSuperTypes: List[Type] = List()
    def byteCodeName(): String
    def loadCode(ch: CodeHandler, index: Int): CodeHandler
    def storeCode(ch: CodeHandler, index: Int): CodeHandler
    def arrayLoadCode(ch: CodeHandler): CodeHandler
    def arrayStoreCode(ch: CodeHandler): CodeHandler
    def returnCode(ch: CodeHandler): CodeHandler
    def cmpEqCode(ch: CodeHandler, id: String): CodeHandler
    def cmpNeCode(ch: CodeHandler, id: String): CodeHandler
    def defaultConstantCode(ch: CodeHandler): CodeHandler

  }

  case object TError extends Type {
    override def isSubTypeOf(tpe: Type): Boolean = true
    override def toString = "[error]"
    override def byteCodeName(): String = "ERROR"
    override def loadCode(ch: CodeHandler, index: Int) = ch
    override def storeCode(ch: CodeHandler, index: Int) = ch
    override def arrayLoadCode(ch: CodeHandler) = ch
    override def arrayStoreCode(ch: CodeHandler) = ch
    override def returnCode(ch: CodeHandler) = ch
    override def cmpEqCode(ch: CodeHandler, id: String) = ch
    override def cmpNeCode(ch: CodeHandler, id: String) = ch
    override def defaultConstantCode(ch: CodeHandler) = ch
  }

  case object TUntyped extends Type {
    override def isSubTypeOf(tpe: Type): Boolean = false
    override def toString = "[untyped]"
    override def byteCodeName(): String = "UNTYPED"
    override def loadCode(ch: CodeHandler, index: Int) = ch
    override def storeCode(ch: CodeHandler, index: Int) = ch
    override def arrayLoadCode(ch: CodeHandler) = ch
    override def arrayStoreCode(ch: CodeHandler) = ch
    override def returnCode(ch: CodeHandler) = ch
    override def cmpEqCode(ch: CodeHandler, id: String) = ch
    override def cmpNeCode(ch: CodeHandler, id: String) = ch
    override def defaultConstantCode(ch: CodeHandler) = ch
  }

  case object TUnit extends Type {
    override def toString = "Unit"
    override def byteCodeName(): String = "V"
    override def loadCode(ch: CodeHandler, index: Int) = ch
    override def storeCode(ch: CodeHandler, index: Int) = ch
    override def arrayLoadCode(ch: CodeHandler) = ch
    override def arrayStoreCode(ch: CodeHandler) = ch
    override def returnCode(ch: CodeHandler) = ch << RETURN
    override def cmpEqCode(ch: CodeHandler, id: String) = ch
    override def cmpNeCode(ch: CodeHandler, id: String) = ch
    override def defaultConstantCode(ch: CodeHandler) = ch
  }

  case object TInt extends Type {
    override def toString = "Int"
    override def byteCodeName(): String = "I"
    override def loadCode(ch: CodeHandler, index: Int) = ch << ILoad(index)
    override def storeCode(ch: CodeHandler, index: Int) = ch << IStore(index)
    override def arrayLoadCode(ch: CodeHandler) = ch << IALOAD
    override def arrayStoreCode(ch: CodeHandler) = ch << IASTORE
    override def returnCode(ch: CodeHandler) = ch << IRETURN
    override def cmpEqCode(ch: CodeHandler, id: String) = ch << If_ICmpEq(id)
    override def cmpNeCode(ch: CodeHandler, id: String) = ch << If_ICmpNe(id)
    override def defaultConstantCode(ch: CodeHandler) = ch << Ldc(0)
  }

  case object TBool extends Type {
    override def toString = "Bool"
    override def byteCodeName(): String = "Z"
    override def loadCode(ch: CodeHandler, index: Int) = ch << ILoad(index)
    override def storeCode(ch: CodeHandler, index: Int) = ch << IStore(index)
    override def arrayLoadCode(ch: CodeHandler) = ch << BALOAD
    override def arrayStoreCode(ch: CodeHandler) = ch << BASTORE
    override def returnCode(ch: CodeHandler) = ch << IRETURN
    override def cmpEqCode(ch: CodeHandler, id: String) = ch << If_ICmpEq(id)
    override def cmpNeCode(ch: CodeHandler, id: String) = ch << If_ICmpNe(id)
    override def defaultConstantCode(ch: CodeHandler) = ch << Ldc(0)
  }

  case object TString extends Type {
    override def toString = "String"
    override def byteCodeName(): String = "Ljava/lang/String;"
    override def loadCode(ch: CodeHandler, index: Int) = ch << ALoad(index)
    override def storeCode(ch: CodeHandler, index: Int) = ch << AStore(index)
    override def arrayLoadCode(ch: CodeHandler) = ch << AALOAD
    override def arrayStoreCode(ch: CodeHandler) = ch << AASTORE
    override def returnCode(ch: CodeHandler) = ch << ARETURN
    override def cmpEqCode(ch: CodeHandler, id: String) = ch << If_ACmpEq(id)
    override def cmpNeCode(ch: CodeHandler, id: String) = ch << If_ACmpNe(id)
    override def defaultConstantCode(ch: CodeHandler) = ch << ACONST_NULL
  }

  case class TArray(tpe: Type) extends Type {
    override def isSubTypeOf(otherTpe: Type): Boolean = {
      otherTpe match {
        case TArray(arrTpe) => tpe.isSubTypeOf(arrTpe)
        case _ => false
      }
    }
    override def toString = tpe.toString + "[]"
    override def byteCodeName(): String = "[" + tpe.byteCodeName
    override def loadCode(ch: CodeHandler, index: Int) = ch << ALoad(index)
    override def storeCode(ch: CodeHandler, index: Int) = ch << AStore(index)
    override def arrayLoadCode(ch: CodeHandler) = ???
    override def arrayStoreCode(ch: CodeHandler) = ???
    override def returnCode(ch: CodeHandler) = ch << ARETURN
    override def cmpEqCode(ch: CodeHandler, id: String) = ch << If_ACmpEq(id)
    override def cmpNeCode(ch: CodeHandler, id: String) = ch << If_ACmpNe(id)
    override def defaultConstantCode(ch: CodeHandler) = ch << ACONST_NULL
  }

  case class TObject(classSymbol: ClassSymbol) extends Type {
    override def isSubTypeOf(tpe: Type): Boolean = tpe match {
      case obj @ TObject(c) =>
        if (classSymbol.name == c.name || c.name == anyObject.classSymbol.name) true
        else classSymbol.parent match {
          case Some(x) => x.getType.isSubTypeOf(tpe)
          case None    => false
        }
      case _ => false
    }
    override def getSuperTypes: List[Type] =
      List(this) ++ (classSymbol.parent match {
        case Some(parentSymbol) => parentSymbol.getType.getSuperTypes
        case None => List()
      })

    override def toString = classSymbol.name
    override def byteCodeName(): String = {
      val name = if (this == anyObject) CodeGeneration.OBJECT else classSymbol.name
      "L" + name + ";"
    }
    def ==(other: TObject): Boolean = classSymbol.name == other.classSymbol.name

    override def loadCode(ch: CodeHandler, index: Int) = ch << ALoad(index)
    override def storeCode(ch: CodeHandler, index: Int) = ch << AStore(index)
    override def arrayLoadCode(ch: CodeHandler) = ch << AALOAD
    override def arrayStoreCode(ch: CodeHandler) = ch << AASTORE
    override def returnCode(ch: CodeHandler) = ch << ARETURN
    override def cmpEqCode(ch: CodeHandler, id: String) = ch << If_ACmpEq(id)
    override def cmpNeCode(ch: CodeHandler, id: String) = ch << If_ACmpNe(id)
    override def defaultConstantCode(ch: CodeHandler) = ch << ACONST_NULL
  }

  // special object to implement the fact that all objects are its subclasses
  val anyObject = TObject(new ClassSymbol("Object"))
}
