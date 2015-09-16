(ns daifu.diagnosis.jurisdiction-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.jurisdiction :as jurisdiction]))

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
