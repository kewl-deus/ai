import com.fasterxml.jackson.annotation.JsonProperty
import org.datavec.api.writable.{FloatWritable, IntWritable}

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
}