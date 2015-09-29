(ns daifu.diagnosis.jurisdiction-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.jurisdiction :refer :all]))
 
^{:refer daifu.diagnosis.jurisdiction/jurisdiction :added "0.1"}
(fact "creates a jurisdiction"

  (-> (jurisdiction {:id :default
                     :type :project})
      jurisdiction?)
  => true)

^{:refer daifu.diagnosis.jurisdiction/jurisdiction? :added "0.1"}
(fact "checks if the object is of type jurisdiction"

  (-> {:id :default
       :type :project}
      jurisdiction?)
  => false)





(comment21

  (require '[clojure.java.io :as io])
  (str (io/as-url (io/file ".")))
  
  
  (require '[hydrox.core :as doc])

  (def reg (doc/single-use))

  (doc/import-docstring reg)
  
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


(comment

  (slurp (retrieve-file (git/repository) {:path "project.clj"}))

  (pick-files (io/file ".") {:type :project})

  (pick-files (git/repository) {:type :project})

  (pick-files (git/repository)
              {:type :project
               :current  {:branch "master"
                          :commit "HEAD"}
               :previous {:branch "master"
                          :commit "HEAD^2"}
               :comparison true
               })

  (list-files (git/repository) {:current  {:branch "master"
                                           :commit "HEAD"}
                                :previous {:branch "master"
                                           :commit "HEAD^2"}
                                :comparison true
                                :source-paths ["src"]})

  (list-files (git/repository) {:current  {:branch "master"
                                           :commit "HEAD"}
                                :previous {:branch "master"
                                           :commit "HEAD"}
                                :comparison true
                                :source-paths ["src"]})



  (pick-files (git/repository)
              {:type :project})

  (pick-files (git/repository)
              {:type :multi
               :patterns [#"daifu"]})

  (pick-files (git/repository)
              {:type :file
               :path "src/daifu/core.clj"
               :comparison true})

  (jurisdiction {:id :default
                 :type :project
                 :version  {:branch nil
                            :commit nil}
                 :previous {:branch nil
                            :commit nil}}))
