
import java.util

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.datavec.api.io.WritableComparable
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.io.ClassPathResource
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.collection.JavaConverters._

object IrisClassfication {

  def main(args: Array[String])  {

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
    val trainIter = createIrisDataIterator("iris.json")
    for (i <- 0 until epochs) {
      model.fit(trainIter)
    }

    println("Evaluating model...")
    val testIter = createIrisDataIterator("iris-testing.json")
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
    println(prediction)
  }


  def createIrisDataIterator(filePath: String, batchSize: Int = 50) = {
    val dataCollection = readIrisData(filePath).map(_.asWriteableCollection.asJava).toList.asJava
    val reader = new CollectionRecordReader(dataCollection)
    new RecordReaderDataSetIterator(reader, batchSize, 4, 3)
  }

  def readIrisData(filePath: String): Array[IrisData] = {
    /*
    val jsonFieldSelection: FieldSelection = new FieldSelection.Builder()
      .addField("sepal_length", "sepal_width", "petal_length", "petal_width", "species")
      .build()
    val trainDataReader = new JacksonRecordReader(jsonFieldSelection, new ObjectMapper(new JsonFactory()))
    val irisTrainDataInput = new InputStreamInputSplit(irisTrainDataResource.getInputStream, irisTrainDataResource.getFile)
    trainDataReader.initialize(irisTrainDataInput)
    */

    val jsonMapper = new ObjectMapper()
    val jsonFactory = new JsonFactory()
    val irisTrainDataResource = new ClassPathResource(filePath)
    val irisData: Array[IrisData] = jsonMapper.readValue(jsonFactory.createParser(irisTrainDataResource.getInputStream), new TypeReference[Array[IrisData]] {})
    return irisData
  }

}
