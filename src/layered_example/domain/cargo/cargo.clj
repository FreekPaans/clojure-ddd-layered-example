(ns layered-example.domain.cargo.cargo)

(defrecord Cargo [cargo-id size])

(defn make-cargo [& {:keys [cargo-id size] :as cargo-data}]
  {:pre [(integer? cargo-id)
         (integer? size)]}
  cargo-data)
  ;(map->Cargo cargo-data))
