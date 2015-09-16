(ns daifu.diagnosis-test
  (:require [daifu.diagnosis :as diagnosis]))

(comment
  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/idiom/arithmatic.indi")))
   {:id :default-project
    :type :project})
  {:indicator :arithmatic, :jurisdisction :default-project, :stat 1, :results [{:path "src/daifu/core/visitation.clj", :stat 0, :results []} {:path "src/daifu/core.clj", :stat 0, :results []} {:path "src/daifu/diagnosis/indicator/idiom.clj", :stat 0, :results []} {:path "src/daifu/diagnosis/indicator.clj", :stat 1, :results [{:expr (+ 1 1), :alt (inc 1), :row 33, :col 3}]} {:path "src/daifu/diagnosis/jurisdiction.clj", :stat 0, :results []} {:path "src/daifu/diagnosis.clj", :stat 0, :results []}]}

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/idiom/sequence.indi")))
   {:id :default-project
    :type :project})

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/form/record_count.indi")))
   {:id :default-project
    :type :project})


  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/file/line_count.indi")))
   {:id :default-project
    :type :project})


  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/function/no_docstring.indi")))
   {:id :default-project
    :type :project})

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/function/token_count.indi")))
   {:id :default-project
    :type :project})

  (diagnose
   (io/file ".")
   (indicator/indicator
    (read-string (slurp "resources/daifu/defaults/indicators/project/project_meta.indi")))
   {:id :default-project
    :type :project})

  )
