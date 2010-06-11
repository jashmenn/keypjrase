(ns keypjrase.instance
  (:require [clojure.contrib [str-utils :as s]]) 
  (:use [clojure.contrib.seq-utils :only [find-first indexed frequencies]])
  (:require [keypjrase [document :as d]] :reload))

(def *training?* false)

(defstruct instance :token :class :features)
(defstruct features :distance :tfidf :pos)

(defn tf-idf
  [phrase local-counts stats]
  (let [local-val (local-counts phrase)
        global-val (stem-global-phrase-count )]
  0))

(defn calculate-phrase-features
  [phrase document local-counts stats] 
  (let [tfidf (tf-idf document local-counts stats)]
  tfidf))
(calculate-phrase-features "flea" d/test-document d/test-frequencies d/test-stats)

(defn calculate-document-instances
  [document stats]
  (let [document-counts (d/body-frequencies document)]
  (map #(apply calculate-phrase-features 
               [% document local-counts stats])
  (d/potential-phrases document))))

(defn create-instances-w-docs
  "given documents"
  [documents stats]
  (map #(apply calculate-document-instances [% stats]) documents))

  ; tfidf (tfidf document document-counts stats training?)
  ; document-counts))


(comment

  (create-instances-w-docs d/test-documents d/test-stats)

  (calculate-features d/test-document d/test-stats true)


  )
