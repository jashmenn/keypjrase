(ns keypjrase.bayes
  (:use [clojure.contrib.generic.math-functions :only [log]])
  (:require [keypjrase [document :as d] [instance :as i]
                       [classifier :as classifier]] :reload)
  (:use [clj-ml.data])
  (:use [clj-ml.filters])
  (:import [java.io File FileOutputStream FileInputStream 
            BufferedInputStream BufferedOutputStream 
            ObjectInputStream ObjectOutputStream])
  (:import [weka.filters Filter])
  (:import [weka.classifiers Classifier])
  (:import [weka.filters.supervised.attribute Discretize]))

(defstruct classifier :cutpoints)

(comment
  "this code is here for when we eventually implement our own classifier"
  )


(comment discretization)

(defn discretize-filter [ds index]
  (make-filter :supervised-discretize {:dataset-format ds :attributes [index]}))

(defn discretize-column!
  "returns a vec [new-dataset cutpoints] (destructive)"
  [dataset column]
  (let [filt (discretize-filter dataset column)
        disc-ds (filter-apply filt dataset)]
   [disc-ds (seq (.getCutPoints filt column))]))

(defn discretize!
  [dataset columns]
  (loop [ds dataset 
         column (first columns)
         others (rest columns)
         cut-points {}]
    (if column 
        (let [[new-ds added-cut-points] (discretize-column! ds column)
              new-cut-points (merge cut-points {column added-cut-points})]
        (recur new-ds (first others) (rest others) new-cut-points))
        [ds cut-points])))

(defn build-classifier-obj []
  (new weka.classifiers.bayes.NaiveBayesSimple))

(defn build [instances]
  (let [ds (classifier/build-dataset-from-instances instances)
        [disc-ds cut-points] (discretize! ds [1 2]) ; todo refactor out
        classifier (build-classifier-obj)]
    (do
      (.buildClassifier classifier disc-ds)
      classifier)))

(comment test data)

(def test-dataset (classifier/build-dataset-from-instances i/test-instances))

(comment

(discretize! test-dataset [1 2])

(build i/test-instances)

  )
