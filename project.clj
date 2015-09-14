(defproject helpshift/daifu "0.1.0-SNAPSHOT"
  :description "checkups for your code"
  :url "https://www.github.com/helpshift/daifu"
  :license {:id "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [im.chit/hara.namespace.eval "2.2.11"]
                 [jonase/kibit "0.1.2"]
                 [im.chit/gita "0.2.0"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]
                                  [leiningen "2.5.2"]
                                  [com.cemerick/pomegranate "0.3.0"]
                                  [helpshift/hydrox "0.1.3"]]
                   :plugins [[lein-midje "3.1.3"]]}}
  :documentation {:site  "daifu"
                 :output "docs"
                 :template {:path "template"
                            :copy ["assets"]
                            :defaults {:template     "article.html"
                                       :navbar       [:file "partials/navbar.html"]
                                       :dependencies [:file "partials/deps-web.html"]
                                       :navigation   :navigation
                                       :article      :article}}
                 :paths ["test/documentation"]
                 :files {"index"
                         {:input "test/documentation/daifu_guide.clj"
                          :title "daifu"
                          :subtitle "checkups for your code"}}
                 :link {:auto-tag    true
                        :auto-number  true}})
