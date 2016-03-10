(ns layered-example.domain.cargo.cargo-repository
  (:refer-clojure :exclude [find]))

(def find nil)
(def add! nil)
(def update! nil)

(defprotocol CargoRepository
  (-find [this cargo-id])
  (-add! [this a-cargo])
  (-update! [this concurrency-version a-cargo]))

(defn set-implementation! [impl]
  (def find (partial -find impl))
  (def add! (partial -add! impl))
  (def update! (partial -update! impl)))
