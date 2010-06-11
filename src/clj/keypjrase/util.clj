(ns keypjrase.util
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
