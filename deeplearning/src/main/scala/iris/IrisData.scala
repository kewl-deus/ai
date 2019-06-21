package iris

import com.fasterxml.jackson.annotation.JsonProperty
import org.datavec.api.writable.{FloatWritable, IntWritable}
import org.nd4j.linalg.dataset.api.DataSet
import org.nd4j.linalg.factory.Nd4j

case class IrisData(@JsonProperty("sepal_length") sepalLength: Float,
                    @JsonProperty("sepal_width") sepalWidth: Float,
                    @JsonProperty("petal_length") petalLength: Float,
                    @JsonProperty("petal_width") petalWidth: Float,
                    @JsonProperty("species") species: String) {

  def asWriteableCollection: Seq[org.datavec.api.writable.Writable] = List(
    new FloatWritable(sepalLength),
    new FloatWritable(sepalWidth),
    new FloatWritable(petalLength),
    new FloatWritable(petalWidth),
    new IntWritable(speciesClassfication)
  )

  private lazy val speciesClassfication: Int = species match {
    case "setosa" => 0
    case "virginica" => 1
    case "versicolor" => 2
  }

  private lazy val speciesClassficationVector: Array[Int] = species match {
    case "setosa" => Array(1, 0, 0)
    case "virginica" => Array(0, 1, 0)
    case "versicolor" => Array(0, 0, 1)
  }

  def asDataSet: DataSet = {
    val features = Nd4j.create(Array(sepalLength, sepalWidth, petalLength, petalWidth), Array(1, 4))
    val labels = Nd4j.create(speciesClassficationVector, Array(1, 3))
    new org.nd4j.linalg.dataset.DataSet(features, labels)
  }
}
