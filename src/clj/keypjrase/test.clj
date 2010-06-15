(ns keypjrase.test
  (:require clojure.contrib.str-utils)
  (:require [clojure.contrib [map-utils :as m]])
  (:require [keypjrase [parser :as parser] 
              [document :as d] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use clojure.contrib.command-line)
  (:gen-class))

(defn classify-instance [instance classifier]
  (let [ds (classifier/build-dataset-from-instances [instance])]
    ; here, how do we filter the instance?
    ; do we add it to the classifier's existing dataset? can we convert it?
    ; then we need to call classify instance on it. note that it must belong to an dataset
    ; see 
  )

(defn predict-tags 
  "given list of instance-structs, returns a list of predicted tags and their
  probabilities"
  [instances classifier]
  (reduce 
    (fn [acc instance] (cons (classify-instance instance classifier) acc))
    '() instances))

(defn test-a-document [document stats classifier]
  (let [instances (instance/create-instances-w-docs [document] stats)
        predicted-tags (predict-tags instances classifier)]
  instances))
(test-a-document d/test-document test-stats test-classifier)

(defn perform-test [documents collection-stats classifier]
  (let [test-stats {}
        at 20] ; calculate stats at 20
    (reduce
      (fn [acc document]
      (m/deep-merge-with + acc 
                         (test-a-document document collection-stats classifier)))
      test-stats documents)))

(defn -test 
  "two things, extraction and test"
  [input-data training-dir output-dir & options]
  (let [opts (merge
               {:parser "tagdoc"}
               (apply hash-map options))
        documents (do (prn "parsing docs") 
                    (parser/parse-input (opts :parser) input-data))
        stats (do (prn "reading stats") 
                    (read-data-structure (str training-dir "/stats.clj")))
        classifier (do (prn "reading classifier") 
                     (classifier/restore (str training-dir "/classifier")))
   ]
    (perform-test documents stats classifier)
  ))

(comment 

  (perform-test d/test-documents test-stats test-classifier)

  (test-a-document d/test-document test-stats test-classifier)

  (def test-instance-struct {:token "dog", :class true, :features {:distance 1/10, :tfidf 0.5991464547107982, :pos nil}})

  (def test-stats (read-data-structure "tmp/runs/stats.clj"))
  (def test-classifier (classifier/restore "tmp/runs/classifier"))

  )
