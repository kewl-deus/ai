package records

import org.datavec.api.records.reader.impl.jackson.FieldSelection

import scala.collection.JavaConverters._

class TransformFieldSelection(private val fieldSelection: FieldSelection)  {

  private var transformations: Map[String, (String) => String] = Map()

  def addTransformation(transformation: (String => String), fieldPath: String): Unit = {
    transformations = transformations + (fieldPath -> transformation)
  }

  def getTransformation(fieldPath: String): Option[(String) => String] = {
    transformations.get(fieldPath)
  }

  def transform(value: String, fieldPath: String): String = {
    getTransformation(fieldPath) match {
      case Some(transformFunc) => transformFunc(value)
      case _ => value
    }
  }

  def getFieldPaths = fieldSelection.getFieldPaths.asScala.toList

  def getValueIfMissing = fieldSelection.getValueIfMissing.asScala.toList

  def getNumFields: Int = fieldSelection.getNumFields
}
