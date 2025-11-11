(ns dev
  (:require [clojure.java.io :as io]
            [com.widdindustries.tiado-cljs2 :as util]
            [babashka.fs :as fs]))


(defn app-config
  ""
  []
  (->
    (util/browser-app-config {:watch-dir  "docs/public"
                              :asset-path "/js"})
    (merge
      {:modules          {:main {:entries ['app.core]}}})))

(defn app-release []
  (util/prod-build
    (-> (app-config)
        (assoc :compiler-options {;:pretty-print true
                                  ;:pseudo-names true
                                  })
        (dissoc :devtools))))

(defn build-docs [_]
  (app-release)
  (System/exit 0))

(comment
  (do
    (util/stop-server)
    (util/clean-shadow-cache)
    (util/clean-dir "docs/public/js")
    (util/watch (app-config)))
  (util/repl)
  :cljs/quit
  (app-release nil)
  (util/build-report (app-config) "build-report.html")

  )