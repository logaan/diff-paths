(ns diff-paths
  (:require [midje.sweet :refer [fact]]
            [clojure.data :refer :all]))

(def before
  {:monkey {:height 50 :velocity 0}
   :pipes  [[50 200] [250 300] [450 400]]
   :last-update #inst "2014-02-08T13:59:35.224-00:00"

   :screen        {:width 480 :height 720}
   :monkey-config {:width 40 :height 30}
   :pipe-config   {:width 100 :gap 120 :height 720}
   :gravity       0.005
   :flapv         -5})

(def after
  {:monkey {:height 60 :velocity 0.05}
   :pipes  [[40 200] [240 300] [440 400]]
   :last-update #inst "2014-02-08T13:59:40.947-00:00"

   :screen        {:width 480 :height 720}
   :monkey-config {:width 40 :height 30}
   :pipe-config   {:width 100 :gap 120 :height 720}
   :gravity       0.005
   :flapv         -5})

(def expected-output
  #{[:monkey]
    [:monkey :height]
    [:monkey :velocity]
    [:pipes]
    [:pipes 0]
    [:pipes 0 0]
    [:pipes 1]
    [:pipes 1 0]
    [:pipes 2]
    [:pipes 2 0]
    [:last-update]})

; Take first two
; Transform them into a set of paths into the data
; Merge those sets
(diff before after)

(defprotocol Path
  (paths [this]))

(extend-protocol Path
  java.lang.Object
  (paths [this] #{})

  clojure.lang.PersistentArrayMap
  (paths [this]
    (set (concat (map list (keys this))
                 (mapcat (fn [[k v]] (map #(conj % k) (paths v))) this))))

  clojure.lang.PersistentVector
  (paths [this]
    (->> (map-indexed vector this)
         (mapcat (fn [[k v]]
                   (conj (map #(conj % k) (paths v))
                         (list k))))
         set)))

(fact (paths {:foo {:baz {:kittens 8 :ducks 9} :so 2} :bar 2})
      => '#{(:foo :baz :kittens) (:foo :so) (:foo) (:bar) (:foo :baz :ducks) (:foo :baz)})

(fact (paths [1]) => '#{[0]})
(fact (paths [1 2]) => '#{[0] [1]})
(fact (paths [[1 2] 3]) => '#{[0] [0 0] [0 1] [1]})

(fact (paths (apply merge (take 2 (diff before after))))
      => expected-output)
