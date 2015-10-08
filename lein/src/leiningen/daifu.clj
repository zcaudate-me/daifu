(ns leiningen.daifu
  "Checkups for your code"
  (:require [daifu.core :as daifu]))

(defn daifu
  "Checkups for your code

   Usage:
   lein daifu -i qa/indicators -o output.edn --format json -f src/example/hello.clj -f src/example/world.clj

   -c, --checkup-path PATH       Path to the checkups file that will be run
   -f, --filter PATH             Path of file to include in analysis
   -i, --indicator-path DIR      Directory to load indicators
   -j, --jurisdiction-path DIR   Directory to load jurisdictions
   -o, --output FILE             Path for output file

   --format FORMAT               Format of output {edn|json}
   --no-defaults                 Do not load default indicators
   --repository PATH             Path to the repository (default to current cwd)
   --use-git                     Load files from git
  "
  [project & args]
  (apply daifu/-main args))
