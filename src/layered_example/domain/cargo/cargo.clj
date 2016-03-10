(ns layered-example.domain.cargo.cargo)

(defrecord Cargo [cargo-id size voyage-id])

(defn make-cargo [& {:keys [cargo-id size] :as cargo-data}]
  {:pre [(integer? cargo-id)
         (integer? size)]}
  (map->Cargo cargo-data))

(defn book-onto-voyage [a-cargo & {:keys [voyage-id]}]
  {:pre [(integer? voyage-id)
         (nil? (:voyage-id a-cargo))]}
  (assoc a-cargo :voyage-id voyage-id))
