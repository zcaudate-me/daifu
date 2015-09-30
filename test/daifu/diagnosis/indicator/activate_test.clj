(ns daifu.diagnosis.indicator.activate-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.indicator.activate :refer :all]
            [daifu.diagnosis.indicator :as indicator]
            [rewrite-clj.zip :as zip]))

^{:refer daifu.diagnosis.indicator.activate/create-function :added "0.1"}
(fact "creates a function from the format for defining indicators"

  ((create-function {:id :count-example
                     :type :function
                     :source '(fn [zloc]
                                (count (zip/sexpr zloc)))})
   (zip/of-string "(defn add [])"))
  => 3)

^{:refer daifu.diagnosis.indicator.activate/activation :added "0.1"}
(fact "returns the activation state for an indicator"

  (-> (indicator/indicator {:id :count-example
                            :type :function
                            :source '(fn [zloc]
                                       (count (zip/sexpr zloc)))})
      (activation))
  => :unactivated)

^{:refer daifu.diagnosis.indicator.activate/create-function-helper :added "0.1"}
(fact "the workhorse for creating the indicator main function")

^{:refer daifu.diagnosis.indicator.activate/activate :added "0.1"}
(fact "activates an indicator for use for invocation"

  (-> (indicator/indicator {:id :count-example
                            :type :function
                            :source '(fn [zloc]
                                       (count (zip/sexpr zloc)))})
      (activate)
      (activation))
  => :activated)

^{:refer daifu.diagnosis.indicator.activate/compiled-rules :added "0.1"}
(fact "creates the kibit form for compiled-rules"

  (-> (indicator/indicator {:id    :plus-one
                  :type  :idiom
                  :rules '[[(+ ?x 1) (inc ?x)]]})
      (compiled-rules))
  => '[(kibit.rules.util/compile-rule (quote [(+ ?x 1) (inc ?x)]))])

^{:refer daifu.diagnosis.indicator.activate/convert-meta :added "0.1"}
(fact "converts the kibit meta to be daifu compatible"

  (convert-meta {:line 3 :column 4})
  => {:row 3, :col 4})
