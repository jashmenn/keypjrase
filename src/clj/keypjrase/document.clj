(ns keypjrase.document
  (:use [keypjrase.stemmer] :reload)
  (:require clojure.contrib.str-utils))

(defstruct document :url :body :tags)
(defstruct collection-stats :df :num-documents)

(defn calculate-collection-stats 
  "given a list of documents, returns a collection-stats struct"
  [documents]
  (let [docfreq (df documents)
        num-docs (count documents)]
  (struct-map collection-stats :df docfreq :num-documents num-docs)))

(defn count-each-word-once [coll]
 (reduce #(merge-with + %1 {(stem %2) 1}) {} coll)) ; ew, stemming twice

(defn df 
  "given documents returns a map where the key is the token and the value is
  the document freq."
  [documents] 
  (reduce
    (fn [acc document] 
      (reduce 
       (fn [acc token]
         (let [stemmed-token (stem token)]
         (assoc acc stemmed-token (+ (get acc stemmed-token 0) 1))))
       acc (keys (count-each-word-once (document :body)))))
    {} documents))

(def test-documents
  [(struct-map document :body '("free" "money" "here" "dog")
                        :tags '("free" "money"))
   (struct-map document :body '("horse" "likes" "eat" "carrots")
                        :tags '("horse" "carrot"))
   (struct-map document :body '("dog" "fleas" "still" "dog" "even" 
                               "though" "go" "flea" "market" "ya"), 
                        :tags '("dog" "flea"))])

(comment

  (calculate-collection-stats test-documents)
  
  (df test-documents)

  )
