package T::lang

extension T::lang::Long {

    Def static MaxValue(): Long = java::lang::Long.MAX_VALUE
    Def static MinValue(): Long = java::lang::Long.MIN_VALUE
    Def static Size(): Int      = java::lang::Long.SIZE
    Def static Bytes(): Int     = java::lang::Long.BYTES

    Def BitsToDouble(): Double           = java::lang::Double.longBitsToDouble(this)
    Def BitCount(): Long                 = java::lang::Long.bitCount(this)
    Def HighestOneBit(): Long            = java::lang::Long.highestOneBit(this)
    Def LowestOneBit(): Long             = java::lang::Long.lowestOneBit(this)
    Def NumberOfLeadingZeros(): Int      = java::lang::Long.numberOfLeadingZeros(this)
    Def NumberOfTrailingZeros(): Int     = java::lang::Long.numberOfTrailingZeros(this)
    Def Reverse(): Long                  = java::lang::Long.reverse(this)
    Def ReverseBytes(): Long             = java::lang::Long.reverseBytes(this)
    Def RotateLeft(distance: Int): Long  = java::lang::Long.rotateLeft(this, distance)
    Def RotateRight(distance: Int): Long = java::lang::Long.rotateRight(this, distance)
    Def Sign(): Long                     = java::lang::Long.signum(this)
    Def ToBinaryString(): String         = java::lang::Long.toBinaryString(this)
    Def ToHexString(): String            = java::lang::Long.toHexString(this)
    Def ToOctalString(): String          = java::lang::Long.toOctalString(this)

    Def toString(): String = java::lang::Long.toString(this)

    Def [](index: Int): Int = {
        if(index > 63 || index < 0)
            error("Index out of bounds: " + index)

        (this & (1L << index)) != 0 ? 1 : 0
    }

}