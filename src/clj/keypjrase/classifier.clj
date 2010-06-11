(ns keypjrase.classifier
  (:use [clojure.contrib.generic.math-functions :only [log]])
  (:require [keypjrase [document :as d] [instance :as i]] :reload)
  (:use [clj-ml.data])
  (:import [weka.filters Filter])
  (:import [weka.filters.supervised.attribute Discretize])
  (:import [weka.classifiers Classifier])
  (:import [weka.classifiers.meta FilteredClassifier]))

; todo, build our own classifier. for now just use weka / clj-ml

(defn build [instances]

  )

(defn build-dataset-from-instances [instances]
  (let [instance-vecs (map i/to-instance-vec instances)
       dataset (make-dataset "tokens" i/dataset-fields instance-vecs)
       labeled-dataset (dataset-set-class dataset 0)]
    labeled-dataset))

(defn build-classifier-obj []
  (let [fclass (new FilteredClassifier)]
    (do
      (.setClassifier fclass (new weka.classifiers.bayes.NaiveBayesSimple))
      (.setFilter fclass (new Discretize))
      fclass)))

(comment 

  (build-dataset-from-instances i/test-instances)

  )
