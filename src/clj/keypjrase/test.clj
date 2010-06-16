(ns keypjrase.test
  (:require clojure.contrib.str-utils)
  (:require [clojure.contrib [map-utils :as m]])
  (:require [keypjrase [parser :as parser] 
              [document :as d] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use [clj-ml.filters])
  (:use clojure.contrib.command-line)
  (:gen-class))

; todo, generalize and move back to weka-ml
(defn class-double-to-name 
  "weka stores an instance class as a double. convert back to a name unless it is true/false, then it is converted to boolean" ; todo memoize
  [attr-value ds]
  (let [classAttribute (.classAttribute ds)
        value (.value classAttribute attr-value)]
    (if (= "true" value)
        true
        (if (= "false" value)
        false
        value) 
    )))

(defn select-best-class
  [distribution ds]
  (reduce (fn [[best-klass best-val] [key val]]
    (if (> val best-val)
      [(class-double-to-name key ds) val]
      [best-klass best-val])
   ) [false neg-inf] (zipmap (range 0 (count distribution)) distribution))) ; built in for this?

(defn classify-instance 
  "returns a vec of the form [predicted-class-name predicted-distribution]"
  [instance classifier]
  (let [ds (classifier/build-dataset-from-instances [instance])
        filtered-ds (filter-apply (.getFilter classifier) ds)
        ; klass (.classifyInstance classifier (.firstInstance filtered-ds))
        dist (seq (.distributionForInstance classifier (.firstInstance filtered-ds)))]
    (select-best-class dist ds)
  ))

(defn predict-instances
  "given a set of unpredicted instances and a classifier, gives predictions for each instance"
  [instances classifier]
  (reduce 
    (fn [acc instance] 
      (let [[predicted-class predicted-prob] (classify-instance instance classifier)
            new-instance (merge instance {:predicted-class predicted-class :predicted-probability predicted-prob})]
      (cons new-instance acc)))
    '() instances))

(defn predict-tags 
  "given list of instance-structs, returns a list of predicted tags and their
  probabilities"
  [instances classifier]
  (let [predicted (predict-instances instances classifier)
        ; tag-probs (remove (fn [instance] (= (instance :predicted-class) false)) predicted)
        tag-probs predicted
        ]
    tag-probs))

; (defn calculate-document-test-stats
;   [document]
;   (let [
;         actual (set (entry :tags))
;         right (intersection actual predicted)
;         num-right (count right)
; 
;         ]
;
;     ))

(defn test-a-document [document stats classifier]
  (let [instances (instance/create-instances-w-docs [document] stats)
        predicted-tags (predict-tags instances classifier)]
  predicted-tags))
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
