package iris

import org.datavec.api.records.reader.impl.jackson.FieldSelection
import org.datavec.api.split.InputStreamInputSplit
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.io.ClassPathResource
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import records.{JsonArrayRecordReader, TransformFieldSelection}
import scala.collection.JavaConverters._

object IrisClassfication {

  def main(args: Array[String]) {

    val seed = 123
    val learningRate = .06
    val batchSize = 50
    val epochs = 100


    val networkConfig = new NeuralNetConfiguration.Builder()
      .updater(new Adam(learningRate))
      .list()
      .layer(0, new DenseLayer.Builder()
        .nIn(4)
        .nOut(5)
        .activation(Activation.SIGMOID)
        .weightInit(WeightInit.SIGMOID_UNIFORM)
        .build())
      .layer(1, new OutputLayer.Builder(LossFunction.MSE)
        .nIn(5)
        .nOut(3)
        .activation(Activation.SIGMOID)
        .weightInit(WeightInit.SIGMOID_UNIFORM)
        .build())
      .build()

    println(networkConfig.toYaml)

    val model = new MultiLayerNetwork(networkConfig)
    model.init()
    model.setListeners(new ScoreIterationListener(10))

    //train
    val trainIter = createIrisDataIterator("iris/iris.json", batchSize)
    for (epCounter <- 0 until epochs) {
      println(s"Train epoch #$epCounter")
      trainIter.reset()
      model.fit(trainIter)
    }

    println("Evaluating model...")
    val testIter = createIrisDataIterator("iris/iris-testing.json", 1)
    val eval = new Evaluation(3)
    testIter.asScala.foreach(ds => {
      val features = ds.getFeatures
      val labels = ds.getLabels
      val predicted = model.output(features, false)
      eval.eval(labels, predicted)
    })
    println(eval.stats())

    testIter.reset()
    val ds = testIter.next()
    ds.setLabelNames(List("setosa", "virginica", "versicolor").asJava)
    val prediction = model.predict(ds)
    println(s"Prediction: $prediction")
  }

  def createIrisDataIterator(filePath: String, batchSize: Int) = {
    val jsonFieldSelection: FieldSelection = new FieldSelection.Builder()
      .addField("sepal_length")
      .addField("sepal_width")
      .addField("petal_length")
      .addField("petal_width")
      .addField("species")
      .build()

    val transformFieldSelection = new TransformFieldSelection(jsonFieldSelection)
    transformFieldSelection.addTransformation(species => species match {
      case "setosa" => "0"
      case "virginica" => "1"
      case "versicolor" => "2"
    }, "species")

    val jsonReader = new JsonArrayRecordReader(transformFieldSelection)
    val irisDataResource = new ClassPathResource(filePath)
    val irisDataInput = new InputStreamInputSplit(irisDataResource.getInputStream, irisDataResource.getURI)

    jsonReader.initialize(irisDataInput)

    new RecordReaderDataSetIterator(jsonReader, batchSize, 4, 3)
  }

  /*
  def createIrisDataIterator(filePath: String, batchSize: Int) = {
    val dataCollection = readIrisData(filePath).map(_.asWriteableCollection.asJava).toList.asJava
    val reader = new CollectionRecordReader(dataCollection)
    new RecordReaderDataSetIterator(reader, batchSize, 4, 3)
  }

  def readIrisData(filePath: String): Array[IrisData] = {
    val jsonMapper = new ObjectMapper()
    val jsonFactory = new JsonFactory()
    val irisDataResource = new ClassPathResource(filePath)
    val irisData: Array[IrisData] = jsonMapper.readValue(jsonFactory.createParser(irisDataResource.getInputStream), new TypeReference[Array[IrisData]] {})
    return irisData
  }
  */
}
