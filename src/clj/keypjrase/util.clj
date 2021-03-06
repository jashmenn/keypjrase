(ns keypjrase.util
  (:import [java.io File FileWriter]) 
  (:use [clojure.core])
  (:require clojure.contrib.str-utils))

; todo should be a macro
(defmacro assoc-unless
  "associates key with val in map unless map contains? key
  (assoc-unless {} :foo 1)       -> {:foo 1}
  (assoc-unless {:foo 0} :foo 1) -> {:foo 0}"
  [map key val]
  (if (contains? map key)
    map
    (assoc map key val)))

(defn map-function-on-map-vals [data f]
  (zipmap (keys data) (map f (vals data))))

(defn save-data-to [file-name data]
 (with-open [w (FileWriter. (File. file-name))]
 (binding [*out* w]
    (prn data))))

(defn read-data-structure [file-name]
  (try
   (let [object (read-string (slurp file-name))]
    object)
   (catch Exception e nil)))

(defmacro apply-str [fname & args] ; doesn't work
  (apply (ns-resolve *ns* (symbol fname)) args))

(def neg-inf java.lang.Float/NEGATIVE_INFINITY)
(def pos-inf java.lang.Float/POSITIVE_INFINITY)

(defn as-percent [numer denom] (* (/ (float numer) (float denom)) 100))

(defmacro if-gt-zero [testval tval fval]
  `(let [testval# ~testval]
    (if (> testval# 0) ~tval ~fval)))

(defn sum [numbers]
  (reduce (fn [acc next] (+ acc next)) 0 numbers))

(defn mean [numbers]
  (/ (sum numbers) (count numbers)))

(defn pop-variance [numbers]
  (let [m (mean numbers)]
    (/ (reduce (fn [acc next] (+ acc (Math/pow (- next m) 2))) 0 numbers)
       (count numbers))))

(defn pop-std-dev [numbers]
  (Math/sqrt (pop-variance numbers)))

(def variance pop-variance)
(def std-dev pop-std-dev)

