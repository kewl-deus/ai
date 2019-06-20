import java.io.DataInputStream
import java.net.URI
import java.util

import org.datavec.api.conf.Configuration
import org.datavec.api.records.Record
import org.datavec.api.records.metadata.RecordMetaData
import org.datavec.api.records.reader.BaseRecordReader
import org.datavec.api.split.InputSplit
import org.datavec.api.writable.Writable

class JsonArrayRecordReader extends BaseRecordReader {

  override def initialize(conf: Configuration, split: InputSplit): Unit = ???

  override def next(): util.List[Writable] = ???

  override def hasNext: Boolean = ???

  override def getLabels: util.List[String] = ???

  override def reset(): Unit = ???

  override def resetSupported(): Boolean = ???

  override def record(uri: URI, dataInputStream: DataInputStream): util.List[Writable] = ???

  override def nextRecord(): Record = ???

  override def loadFromMetaData(recordMetaData: RecordMetaData): Record = ???

  override def loadFromMetaData(recordMetaDatas: util.List[RecordMetaData]): util.List[Record] = ???

  override def close(): Unit = ???

  override def setConf(conf: Configuration): Unit = ???

  override def getConf: Configuration = ???
}
