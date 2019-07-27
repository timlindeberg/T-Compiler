package cafebabe

import cafebabe.ClassFileTypes.{U1, U2, U4}

class AnnotationAttribute(override val attributeNameIndex: U2) extends AttributeInfo(attributeNameIndex, Nil) {
  var annotations: List[AnnotationInfo] = Nil

  override def toStream(stream: ByteStream): ByteStream = {
    val numAnnotations: U2 = annotations.size.asInstanceOf[U2]
    val attributeLength: U4 = 4 * numAnnotations + 2
    stream << attributeNameIndex << attributeLength << numAnnotations << annotations
  }
}

class AnnotationInfo(typeIndex: U2, var elementValuePairs: List[AnnotationElementValue]) extends Streamable {
  override def toStream(stream: ByteStream): ByteStream =
    stream <<
      typeIndex <<
      elementValuePairs.size.asInstanceOf[U2] <<
      elementValuePairs
}

// Currently only supports strings (const_value_index in the JVM spec)
class AnnotationElementValue(tag: U1, nameIndex: U2, valueIndex: U2) extends Streamable {
  override def toStream(stream: ByteStream): ByteStream =
    stream << nameIndex << tag << valueIndex
}
