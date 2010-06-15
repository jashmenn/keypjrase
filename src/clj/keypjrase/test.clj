(ns keypjrase.test
  (:require clojure.contrib.str-utils)
  (:require [clojure.contrib [map-utils :as m]])
  (:require [keypjrase [parser :as parser] 
              [document :as document] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use clojure.contrib.command-line)
  (:gen-class))

(defn test-document [document stats classifier]
  (let [instances  (instance/create-instances-w-docs [document] stats)]
  )

(defn perform-test [documents collection-stats classifier]
  (let [test-stats {}
        at 20] ; calculate stats at 20
    (reduce
      (fn [acc document]
      (m/deep-merge-with + acc 
                         (test-document document collection-stats classifier)))
      test-stats documents))

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
