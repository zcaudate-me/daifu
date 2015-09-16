(ns daifu.diagnosis.indicator-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.indicator :as indicator]))

(comment
  (require '[rewrite-clj.zip :as zip]
           '[clojure.java.io :as io])

  (:main (activate (indicator {:id    :plus-one
                               :type  :idiom
                               :rules '[[(+ ?x 1) (inc ?x)]]})))

  ((indicator {:id    :plus-one
               :type  :idiom
               :rules '[[(+ ?x 1) (inc ?x)]]})
   (io/reader (io/file "src/daifu/diagnosis/indicator.clj")))


  ((indicator {:id :hello-world
               :type :function
               :source '(fn [zloc] (zip/sexpr zloc))})
   (zip/of-string "(+ 1 1)"))



  (./pull-project))
