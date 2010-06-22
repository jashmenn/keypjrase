(ns keypjrase.classifier
  (:use [clojure.contrib.generic.math-functions :only [log]])
  (:require [keypjrase [document :as d] [instance :as i]] :reload)
  (:use [clj-ml.data])
  (:use [clj-ml.filters])
  (:import [java.io File FileOutputStream FileInputStream 
            BufferedInputStream BufferedOutputStream 
            ObjectInputStream ObjectOutputStream])
  (:import [weka.filters Filter])
  (:import [weka.filters.supervised.attribute Discretize])
  (:import [weka.classifiers Classifier])
  (:import [weka.classifiers.meta FilteredClassifier]))

; todo, build our own classifier. for now just use weka / clj-ml

(defn build-dataset-from-instances [instances]
  (let [instance-vecs (map i/to-instance-vec instances)
       dataset (make-dataset "tokens" i/dataset-fields instance-vecs)
       labeled-dataset (dataset-set-class dataset 0)]
    labeled-dataset))

(defn discretize-filter [ds attributes]
  (make-filter :supervised-discretize {:dataset-format ds :attributes attributes}))

(defn normalize-filter [ds opts]
  (make-filter :normalize (merge {:dataset-format ds} opts)))

(defn build-multifilter [ds]
  (let [mf (new weka.filters.MultiFilter)
        filters (into-array weka.filters.Filter 
                            [(normalize-filter ds {:scale 100}) ; tmp
                             (discretize-filter ds [1 2])])]
    (do 
      (.setFilters mf filters)
      mf)))

(defn build-classifier-obj [ds]
  (let [fclass (new FilteredClassifier)]
    (do
      (.setClassifier fclass (new weka.classifiers.bayes.NaiveBayesSimple))
      ; (.setFilter fclass (new Discretize))
      (.setFilter fclass (build-multifilter ds))
      fclass)))

(defn build [instances]
  (let [ds (build-dataset-from-instances instances)
        classifier (build-classifier-obj ds)]
    (do
      (.buildClassifier classifier ds)
      classifier)))

(defn save [classifier out-file]
  (let [buf-out (new BufferedOutputStream (new FileOutputStream out-file))
        out (new ObjectOutputStream buf-out)]
    (do
      (.writeObject out classifier)
      (.flush out)
      (.close out))))

(defn restore [in-file]
  (let [buf-in (new BufferedInputStream (new FileInputStream in-file))
        in (new ObjectInputStream buf-in)
        classifier (.readObject in)]
    (do
      (.close in)
      classifier)))

(comment "test data")
 
(def test-dataset (build-dataset-from-instances i/test-instances))
        

(comment 

  (build-dataset-from-instances i/test-instances)

  (build i/test-instances)

  (let [c (build i/test-instances)]
    (save c "tmp/classifier.dat"))

  (restore "tmp/classifier.dat")

  (discretize-filter test-dataset [1 2])

  )
