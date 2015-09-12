(ns daifu.diagnostic.run
  (:require [kibit.check :as kibit]
            [kibit.rules.util :as rules]))

(comment
  (rules/compile-rule '[(+ ?x 1) (inc ?x)])
  )
