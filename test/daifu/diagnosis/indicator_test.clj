(ns daifu.diagnosis.indicator-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.indicator :refer :all]
            [rewrite-clj.zip :as zip]))

^{:refer daifu.diagnosis.indicator/activate :added "0.1"}
(fact "activates the indicator"

  (-> (indicator {:id :hello-world
                  :type :function
                  :source '(fn [zloc] (zip/sexpr zloc))})
      (activate)
      (activated?))
  => true)

^{:refer daifu.diagnosis.indicator/activated? :added "0.1"}
(fact "checks to see if the indicator is activated"

  (-> (indicator {:id :hello-world
                  :type :function
                  :source '(fn [zloc] (zip/sexpr zloc))})
      
      (activated?))
  => false)

^{:refer daifu.diagnosis.indicator/invoke-indicator :added "0.1"}
(fact "invokes the indicator with given arguments"
  (-> (indicator {:id :hello-world
                                :type :function
                  :source '(fn [zloc] (zip/sexpr zloc))})
      (invoke-indicator (zip/of-string "(+ 1 2 3)")))
  => '(+ 1 2 3))

^{:refer daifu.diagnosis.indicator/indicator :added "0.1"}
(fact "when passed a map, returns an indicator"
  (-> (indicator {:id :hello-world
                  :type :function
                  :source '(fn [zloc] (zip/sexpr zloc))})
      (indicator?))
  => true)

^{:refer daifu.diagnosis.indicator/indicator? :added "0.1"}
(fact "checks if object is in fact in indicator"
  (indicator? {:id :hello-world
               :type :function
               :source '(fn [zloc] (zip/sexpr zloc))})
  => false)

^{:refer daifu.diagnosis.indicator/activate-indicator-helper :added "0.1"}
(fact "activates indicator with project dependencies and injections"

  ((activate-indicator-helper (indicator {:id :file-example
                                           :type :file
                                           :source (fn [f]
                                                     (io/as-relative-path f))})
                              '[[clojure.java.io :as io]])
   (io/file "."))
  => ".")

^{:refer daifu.diagnosis.indicator/compiled-rules :added "0.1"}
(fact "creates the kibit form for compiled-rules"

  (-> (indicator {:id    :plus-one
                  :type  :idiom
                  :rules '[[(+ ?x 1) (inc ?x)]]})
      (compiled-rules))
  => '[(kibit.rules.util/compile-rule (quote [(+ ?x 1) (inc ?x)]))])


^{:refer daifu.diagnosis.indicator/convert-meta :added "0.1"}
(fact "converts the kibit meta to be daifu compatible"

  (convert-meta {:line 3 :column 4})
  => {:row 3, :col 4})
