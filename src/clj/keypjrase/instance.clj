
(ns keypjrase.instance
  (:require [clojure.contrib [str-utils :as s]]) 
  (:use [keypjrase.stemmer] :reload)
  (:use [clojure.contrib.seq-utils :only [find-first indexed]])
  (:use [clojure.contrib.generic.math-functions :only [log]])
  (:require [keypjrase [document :as d]] :reload))

(def *training?* false)

(defstruct instance :token :class :features :predicted-class :predicted-probability)
(defstruct features :distance :tfidf :pos)

(defmacro with-training*
  "Evaluates func in the context of a training"
  [body]
  `(binding [*training?* true]
    (~@body)))

(defn tf-idf
  [phrase document local-counts stats]
  (let [local-val (local-counts phrase)
        global-val (let [g (or (d/global-phrase-count phrase stats) 0)]
                     (if (and (> g 0) *training?*) (- g 1) g))
        length (d/document-length document)
        tf (/ local-val length)
        idf (- (log (/ (+ global-val 1) (+ (stats :num-documents) 1))))]
  (* tf idf)))

(defn calculate-phrase-features
  "this function has so many parameters because we need to calculate many
  features on the whole document. it is much more efficient to calculate them
  once than to calculate them for each phrase"
  [phrase document local-counts first-occur stats] 
  (let [stem-phrase (stem phrase)
        tfidf (tf-idf stem-phrase document local-counts stats)
        first-oc (first-occur stem-phrase)
        klass (d/is-tag? stem-phrase document) ; stemmed?
        features (struct-map features :distance first-oc :tfidf tfidf)
        instance (struct-map instance :token phrase :class klass
                                      :features features)]
  instance))

(defn calculate-document-instances
  [document stats]
  (let [local-counts (d/body-frequencies document)
        first-occur (d/first-occurrences document)]
  (map #(apply calculate-phrase-features 
               [% document local-counts first-occur stats])
  (d/potential-phrases document))))

(defn create-instances-w-docs
  "given documents"
  [documents stats]
  (flatten (map #(apply calculate-document-instances [% stats]) documents)))


(def dataset-fields 
   [{:is_keyword [:true :false]} :tfidf :distance])

(defn to-instance-vec [instance] ; todo grab features dynamically from dataset-fields
  [(keyword (str (instance :class))) 
    ((instance :features) :tfidf) 
    ((instance :features) :distance)])

(def test-instances (create-instances-w-docs d/test-documents d/test-stats))

(comment

   (.printStackTrace *e)

  (create-instances-w-docs d/test-documents d/test-stats)
  (calculate-document-instances d/test-document d/test-stats)

  (calculate-phrase-features "flea" d/test-document d/test-frequencies 
                           d/test-first-occur d/test-stats)


  )
