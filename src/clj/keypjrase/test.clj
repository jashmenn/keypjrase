(ns keypjrase.test
  (:require clojure.contrib.str-utils)
  (:require [clojure.contrib [map-utils :as m]])
  (:require [keypjrase [parser :as parser] 
              [document :as d] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use [keypjrase.extract] :reload)
  (:use [clj-ml.filters])
  (:use [clojure.set])
  (:use clojure.contrib.command-line)
  (:gen-class))

(defn prediction-numbers [predicted actual right] 
  (let [num-right (count right)
    prec (if-gt-zero (count predicted) (/ (count right) (count predicted)) 0)
    rec  (if-gt-zero (count actual) (/ (count right) (count actual)) 1) ; default?
    f    (if-gt-zero (+ prec rec) (* 2 (/ (* prec rec) (+ prec rec))) 0)
    hit  (if-gt-zero num-right 1 0)] ; did we guess any tags right?
  {:precision prec, :recall rec, :fmeasure f, 
   :num-right num-right, :num-possible (count actual), :hit hit} ))

(defn predict-tags 
  "given list of instance-structs, returns a list of predicted tags and their
  probabilities"
  [instances document classifier at]
  (let [predictions (predict-instances instances document classifier 2) ;predictions test-predictions
        all-predicted (filter #(:predicted-class %) predictions)
        predicted (set (map :token (top-n-predicted all-predicted at)))
        (comment "two definitions of 'actual'
          1. the actual tags labeled and 
          2. the tags that are possible to extract from the document itself.
          You can decide which definition you prefer")
        actual (set (map :token (filter #(:class %) predictions)))
        ; actual (document :tags)
        right (intersection actual predicted)]
      (do
        ; (prn (top-n-predicted all-predicted at))
        ; (prn predictions)
      (prn [predicted right actual]) ; todo log
      (merge (prediction-numbers predicted actual right) {:count 1})
      )))

(defn test-a-document [document stats classifier at]
  (let [instances (instance/create-instances-w-docs [document] stats)
        predicted-tags (predict-tags instances document classifier at)]
  predicted-tags))

(defn perform-test [documents collection-stats classifier at] 
  (let [test-stats {}]
    (reduce
      (fn [acc document]
      (m/deep-merge-with + acc 
        (test-a-document document collection-stats classifier at)))
      test-stats documents)))

(defn print-results [t]
  (printf "Results:
  %d/%d (%.2f%%) hit/documents
  %d/%d (%.2f%%) correct/possible tags
  %f precision
  %f recall
  %f f-measure\n"
          (t :hit) (t :count) (as-percent (t :hit) (t :count))
          (t :num-right) (t :num-possible)
            (as-percent (t :num-right) (t :num-possible))
          (float (t :precision)) 
          (float (t :recall)) 
          (float (t :fmeasure))
          ))

(defn -test 
  "two things, extraction and test"
  [input-data training-dir output-dir at & options]
  (let [opts (merge
               {:parser "tagdoc"}
               (apply hash-map options))
        documents (do (prn "parsing docs") 
                    (parser/parse-input (opts :parser) input-data))
        stats (do (prn "reading stats") 
                    (read-data-structure (str training-dir "/stats.clj")))
        classifier (do (prn "reading classifier") 
                     (classifier/restore (str training-dir "/classifier")))
        results (perform-test documents stats classifier at)
   ]
       (print-results results)
  ))

(comment 

  (use 'keypjrase.test)

  (classify-instance test-instance-struct test-classifier)

  (perform-test d/test-documents test-stats test-classifier 20)

  (test-a-document d/test-document test-stats test-classifier 20)

  (predict-instances instance/test-instances d/test-document test-classifier 2)

   (instance-distribution (first instance/test-instances) test-classifier)

  )
