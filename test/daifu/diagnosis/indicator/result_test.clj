(ns daifu.diagnosis.indicator.result-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.indicator.result :refer :all]))

^{:refer daifu.diagnosis.indicator.result/total :added "0.1"}
(fact "calculates the sum of all data points in a list"

  (total [true false true]) => 2
  (total [1 2 3 4]) => 10)

^{:refer daifu.diagnosis.indicator.result/average :added "0.1"}
(fact "calculates the average of data points in a list"

  (average [true false true false]) => 1/2
  (average [1 2 3 4 5]) => 3)


^{:refer daifu.diagnosis.indicator.result/->stat :added "0.1"}
(fact "creates a stat (of type Number) from various data types"
  
  (->stat nil) => 0
  (->stat false) => 0
  (->stat true) => 1
  (->stat 10) => 10
  (->stat [1 2 3]) => 6
  (->stat (result [1 2 3])) => 6)

^{:refer daifu.diagnosis.indicator.result/result? :added "0.1"}
(fact "check to see if data if of type Result"
  
  (result? {:data 1})
  => false)

^{:refer daifu.diagnosis.indicator.result/result :added "0.1"}
(fact "creates a result object from various types"
  
  (result true)
  => (contains {:data true, :stat 1})

  (result 1)
  => (contains {:data 1, :stat 1})
  
  (result {:data 1})
  => (contains {:data 1, :stat 1})

  (result [1 2 3 4 5] average)
  => (contains {:data [1 2 3 4 5], :stat 3}))
