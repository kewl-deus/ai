package records

import java.io.{DataInputStream, InputStreamReader, Reader}
import java.net.URI
import java.util

import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeFactory}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.datavec.api.conf.Configuration
import org.datavec.api.records.Record
import org.datavec.api.records.metadata.{RecordMetaData, RecordMetaDataIndex}
import org.datavec.api.records.reader.BaseRecordReader
import org.datavec.api.split.InputSplit
import org.datavec.api.writable.{Text, Writable}

import scala.collection.JavaConverters._
import scala.collection.immutable

class JsonArrayRecordReader(val selection: TransformFieldSelection, val objectMapper: ObjectMapper = new ObjectMapper()) extends BaseRecordReader {

  /** current index in array */
  private var cursor: Int = 0
  private var jsonArray: ArrayNode = new ArrayNode(new JsonNodeFactory(false))

  override def initialize(conf: Configuration, split: InputSplit): Unit = initialize(split)

  override def initialize(split: InputSplit): Unit = {
    if (split.locations().size > 1) throw new UnsupportedOperationException("Only single location is supported")
    super.initialize(split)

    val uri = split.locations().head
    val inputReader = new InputStreamReader(streamCreatorFn(uri))
    val jsonNode = objectMapper.readTree(inputReader)
    inputReader.close()

    jsonNode match {
      case arrayNode: ArrayNode => jsonArray = arrayNode
      case _ => throw new UnsupportedOperationException("Input does not contain a JSON Array")
    }
    cursor = 0
  }

  override def next(): util.List[Writable] = {
    val result = selectFields(jsonArray.get(cursor))
    cursor += 1
    return result
  }

  override def nextRecord(): Record = {
    val data = next()
    val uri = inputSplit.locations().head
    new org.datavec.api.records.impl.Record(data, new RecordMetaDataIndex(cursor, uri, this.getClass))
  }

  override def hasNext: Boolean = cursor < jsonArray.size()

  override def getLabels: util.List[String] = throw new UnsupportedOperationException

  override def reset(): Unit = {
    cursor = 0
  }

  override def resetSupported(): Boolean = true

  override def record(uri: URI, dataInputStream: DataInputStream): util.List[Writable] = readJson(new InputStreamReader(dataInputStream))

  private def readJson(reader: Reader): util.List[Writable] = {
    val jsonNode = objectMapper.readTree(reader)
    jsonNode match {
      case arrayNode: ArrayNode => {
        val itemNode = arrayNode.get(cursor)
        selectFields(itemNode)
      }
      case _ => selectFields(jsonNode)
    }
  }

  private def selectFields(jsonNode: JsonNode): util.List[Writable] = {
    val paths = selection.getFieldPaths
    val valueIfMissing = selection.getValueIfMissing
    val result: immutable.Seq[Writable] = paths.map(path => path.mkString(".")).zip(valueIfMissing).map {
      case (path, defaultValue) => jsonNode.hasNonNull(path) match {
        case true => {
          val str = jsonNode.get(path).asText()
          new Text(selection.transform(str, path))
        }
        case _ => defaultValue
      }
    }
    result.asJava
  }


  override def loadFromMetaData(recordMetaData: RecordMetaData): Record = ???

  override def loadFromMetaData(recordMetaDatas: util.List[RecordMetaData]): util.List[Record] = ???

  override def close(): Unit = {}

  override def setConf(conf: Configuration): Unit = {}

  override def getConf: Configuration = null


}
