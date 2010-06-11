(ns keypjrase.instance
  (:require [clojure.contrib [str-utils :as s]]) 
  (:use [clojure.contrib.seq-utils :only [find-first indexed frequencies]])
  (:use [clojure.contrib.generic.math-functions :only [log]])
  (:require [keypjrase [document :as d]] :reload))

(def *training?* false)

(defstruct instance :token :class :features)
(defstruct features :distance :tfidf :pos)

(defn tf-idf
  [phrase document local-counts stats]
  (let [local-val (local-counts phrase)
        global-val (let [g (or (d/stem-global-phrase-count phrase stats) 0)]
                     (if (and (> g 0) *training?*) (- g 1) g))
        length (d/document-length document)
        tf (/ local-val length)
        idf (- (log (/ (+ global-val 1) (+ (stats :num-documents) 1))))]
  (* tf idf)))
(calculate-phrase-features "flea" d/test-document d/test-frequencies d/test-stats)

(defn calculate-phrase-features
  [phrase document local-counts stats] 
  (let [tfidf (tf-idf phrase document local-counts stats)]
  tfidf))

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
