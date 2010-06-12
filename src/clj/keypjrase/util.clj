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

(defn apply-str [fname & args]
  (apply (ns-resolve *ns* (symbol fname)) args))


