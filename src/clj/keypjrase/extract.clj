(ns keypjrase.extract
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
