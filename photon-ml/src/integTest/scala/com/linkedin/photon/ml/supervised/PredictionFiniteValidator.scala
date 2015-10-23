package com.linkedin.photon.ml.supervised

import com.linkedin.photon.ml.data.LabeledPoint
import com.linkedin.photon.ml.supervised.classification.BinaryClassifier
import com.linkedin.photon.ml.supervised.model.GeneralizedLinearModel
import com.linkedin.photon.ml.supervised.regression.Regression
import org.apache.spark.rdd.RDD

/**
 * Verify that on a particular data set, the model only produces finite predictions
 */
class PredictionFiniteValidator extends ModelValidator[GeneralizedLinearModel] {

  override def validateModelPredictions(model:GeneralizedLinearModel, data:RDD[LabeledPoint]) : Unit = {
    val features = data.map { x => x.features }
    var predictions:RDD[Double] = null
    model match {
      case r:Regression =>
        predictions = r.predictAll(features)

      case b:BinaryClassifier =>
        predictions = b.predictClassAllWithThreshold(features, 0.5)

      case _ =>
        throw new IllegalArgumentException("Don't know how to handle models of type [" + model.getClass.getName + "]")
    }

    val invalidCount = predictions.filter(x => !java.lang.Double.isFinite(x)).count
    if (invalidCount > 0) {
      throw new IllegalStateException("Found [" + invalidCount + "] samples with invalid (NaN or +/-Inf) predictions")
    }
  }
}