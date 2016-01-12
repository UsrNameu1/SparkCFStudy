
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, ALS, Rating}
import org.apache.spark.sql.SQLContext

object Sample extends App {

  val conf: SparkConf = new SparkConf().setMaster("local").setAppName("test")
  val sc: SparkContext = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)
  val df = sqlContext.read
    .format("com.databricks.spark.csv")
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    .load("./resources/RCdata/rating_final.csv")
  val ratings = df.select("userID", "placeID", "rating").rdd.map { row =>
    Rating(row.getString(0).tail.toInt, row.getInt(1), row.getInt(2).toDouble)
  }
  val splitedRatings = ratings.randomSplit(Array(0.9, 0.1), 12345)
  val trainRatings = splitedRatings(0)
  val targetRatings = splitedRatings(1)

  // Build the recommendation model using ALS
  val rank = 10
  val numIterations = 10
  val model = ALS.train(trainRatings, rank, numIterations, 0.01, -1, 123456)

  // Evaluate the model on rating data
  val usersProducts = targetRatings.map { case Rating(user, product, rate) =>
    (user, product)
  }
  val predictions =
    model.predict(usersProducts).map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }
  val ratesAndPreds = targetRatings.map { case Rating(user, product, rate) =>
    ((user, product), rate)
  }.join(predictions)
  val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
    val err = (r1 - r2)
    err * err
  }.mean()
  println("Mean Squared Error = " + MSE)

  // Save and load model
//  model.save(sc, "./target/tmp/myCollaborativeFilter")
//  val sameModel = MatrixFactorizationModel.load(sc, "./target/tmp/myCollaborativeFilter")
//  val predicted = sameModel.predict(1077, 135104)
//  println("predicted rate = " + predicted)
}
