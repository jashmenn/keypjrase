(ns keypjrase.test
  (:require clojure.contrib.str-utils)
  (:require [clojure.contrib [map-utils :as m]])
  (:require [keypjrase [parser :as parser] 
              [document :as d] [instance :as instance]
              [classifier :as classifier]] :reload)
  (:use [keypjrase.util] :reload)
  (:use [clj-ml.filters])
  (:use [clojure.set])
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

; todo, refactor this, its really just picking the true class
(defn select-best-class
  [distribution ds]
  (reduce (fn [[best-klass best-val] [key val]]
    (let [class-name (class-double-to-name key ds)]
    ; (if (> val best-val)
    (if (= class-name true)
      [class-name val]
      [best-klass best-val])
   )) [false neg-inf] (zipmap (range 0 (count distribution)) distribution))) ; built in for this?


(defn instance-distribution [instance classifier]
  (let [ds (classifier/build-dataset-from-instances [instance])
        filtered-ds (filter-apply (.getFilter classifier) ds)
        dist (seq (.distributionForInstance classifier 
                                            (.firstInstance filtered-ds)))]
      [dist filtered-ds]))

; just classify everything as true and then take the top n of those
(defn classify-instance 
  "returns a vec of the form [predicted-class-name predicted-distribution]"
  [instance classifier] ; (select-best-class dist ds)
  (let [[dist ds] (instance-distribution instance classifier)]
    (select-best-class dist ds)
  ))

(defn predict-instance
  [instance classifier]
  (let [[predicted-class predicted-prob] 
           (classify-instance instance classifier)
        new-instance (merge instance 
                            {:predicted-class predicted-class 
                             :predicted-probability predicted-prob})]
    new-instance))

(defn predict-instances
      [instances document classifier min-count]
      (let [freq (d/body-frequencies document)]
           freq
      (reduce  
        (fn [acc instance]
            (if (>= (get freq (instance :token) 0) min-count)
                (cons (predict-instance instance classifier) acc)
                acc))
        '() instances)))

      ; calculate the document frequencies and create a map
      ; for each instance, only calculate the probability of d
      ; instnaces that occur more than the min count. al
      ; when we actually choose one, we only want to predict the probabilityt that it is a tag

(defn top-n-predicted [predicted n]
  (take n 
    (reverse (sort #(compare (%1 :predicted-probability) (%2 :predicted-probability))
          predicted))))

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
        actual (set (map :token (filter #(:class %) predictions)))
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

(comment test-data)

(def test-instance-struct {:token "dog", :class true, 
  :features {:distance 1/10, :tfidf 0.5991464547107982, :pos nil}})
(def test-stats (read-data-structure "tmp/runs/stats.clj"))
(def test-classifier (classifier/restore "tmp/runs/classifier"))
(def test-predictions 
'({:token "ya", :class false, :features 
    {:distance 4/5, :tfidf 0.3506, :pos nil},  
    :predicted-class false, :predicted-probability 0.7243} 
  {:token "market", :class false, :features 
    {:distance 7/10, :tfidf 0.2302, :pos nil}, 
    :predicted-class true, :predicted-probability 0.8243}
  {:token "flea", :class true, :features 
    {:distance 1/5, :tfidf 0.9210, :pos nil},  
    :predicted-class true, :predicted-probability 0.7243} 
  {:token "go", :class false, :features 
    {:distance 3/5, :tfidf 0.1049, :pos nil},  
    :predicted-class false, :predicted-probability 0.2243} 
  {:token "though", :class false, :features 
    {:distance 1/2, :tfidf 0.1469, :pos nil},  
    :predicted-class false, :predicted-probability 0.8243})
)


(comment 

  (use 'keypjrase.test)

  (classify-instance test-instance-struct test-classifier)

  (perform-test d/test-documents test-stats test-classifier 20)

  (test-a-document d/test-document test-stats test-classifier 20)

  (predict-instances instance/test-instances d/test-document test-classifier 2)

   (instance-distribution (first instance/test-instances) test-classifier)

  )
