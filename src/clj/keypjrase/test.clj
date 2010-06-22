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
  (:use clojure.contrib.duck-streams)
  (:gen-class))

(defn prediction-numbers [predicted actual right] 
  (let [num-right (count right)
    prec (if-gt-zero (count predicted) (/ (count right) (count predicted)) 0)
    rec  (if-gt-zero (count actual) (/ (count right) (count actual)) 0) ; default?
    f    (if-gt-zero (+ prec rec) (* 2 (/ (* prec rec) (+ prec rec))) 0)
    hit  (if-gt-zero num-right 1 0)] ; did we guess any tags right?
  {:precision prec, :recall rec, :fmeasure f, 
   :num-right num-right, :num-possible (count actual), :hit hit} ))

(defn generate-prediction-numbers 
  [instances document classifier options]
  (let [predictions (predict-instances instances document classifier 2) ;predictions test-predictions
        all-predicted (filter #(:predicted-class %) predictions)
        ; predicted (set (map :token (top-n-predicted all-predicted (options :at))))
        predicted (set (map :token 
          (top-n-predicted-w-thresh all-predicted options)))
        ; (comment "two definitions of 'actual'
        ;   1. the actual tags labeled and 
        ;   2. the tags that are possible to extract from the document itself
        ;   You can decide which definition you prefer")
        actual (set (map :token (filter #(:class %) predictions)))
        ; actual (document :tags)
        right (intersection actual predicted)]
      (do
        ; (prn (top-n-predicted all-predicted at))
        ; (prn predictions)
      (prn [predicted right actual]) ; todo log
      (merge (prediction-numbers predicted actual right) {:count 1})
      )))

(defn test-a-document [document stats classifier options]
  (let [instances (instance/create-instances-w-docs [document] stats)
        prediction-numbers (generate-prediction-numbers instances document classifier options)]
  prediction-numbers))

(defn postprocess-results [r]
  (merge r
         {:precision (/ (r :precision) (r :count)),
          :recall    (/ (r :recall)    (r :count)),
          :fmeasure  (/ (r :fmeasure)  (r :count))}))

(defn perform-test [documents collection-stats classifier options] 
  (let [results (reduce
          (fn [acc document]
          (m/deep-merge-with + acc 
            (test-a-document document collection-stats classifier options)))
          {} documents)]
  (postprocess-results results)))
; here you need to make the prec/recall/fmeasure be divided by the number of docs 

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
(def csv-header
 "tfidf-thresh,prob-thresh,hit,documents,num-right,num-possible,precision,recall,fmeasure\n")

(defn results-to-csv [t]
  (format "%d,%d,%d,%d,%f,%f,%f"
          (t :hit) (t :count)
          (t :num-right) (t :num-possible)
          (float (t :precision)) 
          (float (t :recall)) 
          (float (t :fmeasure))
          ))

(defn run-test
  [input-data training-dir output-dir options]
  (let [opts (merge
               {:parser "tagdoc",
                :tfidf-threshold 0.0,
                :at 20}
               options)
        documents (do (prn "parsing docs") 
                    (parser/parse-input (opts :parser) input-data))
        stats (do (prn "reading stats") 
                    (read-data-structure (str training-dir "/stats.clj")))
        classifier (do (prn "reading classifier") 
                     (classifier/restore (str training-dir "/classifier")))
        results (perform-test documents stats classifier opts)]
       results))

(defn -test 
  "two things, extraction and test"
  [input-data training-dir output-dir & options]
    (let [opts (merge {} (apply hash-map options))]
      (print-results (run-test input-data training-dir output-dir opts))))

(defn -test-tfidf-threshold
  [input-data training-dir output-dir & options]
  (let [outfile (str output-dir "/results.csv")
        prob-thresh 0.0]
  (do
    (append-spit outfile csv-header) 
    (for [x (range 0.0 0.15 0.02)]
      (let [results (run-test input-data 
                              training-dir 
                              output-dir 
                              (merge options {:tfidf-threshold x}))]
        (append-spit outfile 
                     (str x "," prob-thresh "," (results-to-csv results) "\n")))))))

(defn -test-prob-threshold
  [input-data training-dir output-dir & options]
  (let [outfile (str output-dir "/results.csv")
        tfidf-thresh 0.0]
  (do
    (append-spit outfile csv-header) 
    (for [x (range 0.2 0.3 0.02)]
      (let [results (run-test input-data 
                              training-dir 
                              output-dir 
                              (merge options {:prob-threshold x}))]
        (append-spit outfile 
                     (str tfidf-thresh "," x "," (results-to-csv results) "\n")))))))


(comment 

  (use 'keypjrase.test)

  (classify-instance test-instance-struct test-classifier)

  (perform-test d/test-documents test-stats test-classifier 20)

  (test-a-document d/test-document test-stats test-classifier 20)

  (predict-instances instance/test-instances d/test-document test-classifier 2)

   (instance-distribution (first instance/test-instances) test-classifier)

  )
