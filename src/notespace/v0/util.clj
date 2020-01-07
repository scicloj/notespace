(ns notespace.v0.util
  (:require [clojure.pprint :as pp]
            [com.rpl.specter :refer [MAP-VALS transform]]
            [zprint.core :as zp])
  (:import clojure.lang.IDeref))

(defn pprint-and-return [x]
  (pp/pprint x)
  x)

(defn fmap [f m]
  (transform [MAP-VALS] f m))

(defn only-one [elements]
  (case (count elements)
    0 nil
    1 (first elements)
    (throw (ex-info "Expected only one element."
                    {:elements elements}))))


;; https://stackoverflow.com/questions/58308404/configure-symbol-quote-expansion-in-clojure-zprint
;; https://github.com/kkinnear/zprint/issues/121
(defn careful-zprint [form width]
  (-> form
      clojure.pprint/pprint
      with-out-str
      (zprint.core/zprint width {:parse-string? true})))

(defn deref-if-ideref [v]
  (if (instance? IDeref v)
    @v
    v))

(defn check [pred & args]
  [(if (apply pred args)
    :PASSED
    :FAILED)
   (last args)])
