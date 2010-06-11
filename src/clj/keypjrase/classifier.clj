(ns keypjrase.classifier
  (:use [clojure.contrib.generic.math-functions :only [log]])
  (:require [keypjrase [document :as d] [instance :as i]] :reload)
  (:use [clj-ml.data])
  (:import [java.io File FileOutputStream FileInputStream 
            BufferedInputStream BufferedOutputStream 
            ObjectInputStream ObjectOutputStream])
  (:import [weka.filters Filter])
  (:import [weka.filters.supervised.attribute Discretize])
  (:import [weka.classifiers Classifier])
  (:import [weka.classifiers.meta FilteredClassifier]))

; todo, build our own classifier. for now just use weka / clj-ml

(defn build [instances]
  (let [ds (build-dataset-from-instances instances)
        classifier (build-classifier-obj)]
    (do
      (.buildClassifier classifier ds)
      ; (prn classifier)
      classifier)))

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

(defn save-classifier [classifier out-file]
  (let [buf-out (new BufferedOutputStream (new FileOutputStream out-file))
        out (new ObjectOutputStream buf-out)]
    (do
      (.writeObject out classifier)
      (.flush out)
      (.close out))))

(defn load-classifier [in-file]
  (let [buf-in (new BufferedInputStream (new FileInputStream in-file))
        in (new ObjectInputStream buf-in)
        classifier (.readObject in)]
    (do
      (.close in)
      classifier)))

(comment 

  (build-dataset-from-instances i/test-instances)

  (build i/test-instances)

  (let [c (build i/test-instances)]
    (save-classifier c "tmp/classifier.dat"))

  (load-classifier "tmp/classifier.dat")

  )
