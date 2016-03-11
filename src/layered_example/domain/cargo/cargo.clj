(ns layered-example.domain.cargo.cargo)

(defrecord Cargo [cargo-id size voyage-id])

(defn create-new-cargo [& {:keys [size voyage-id] :as cargo-data}]
  {:pre [(integer? size)
         (nil? voyage-id)]}
  (map->Cargo cargo-data))

(defn book-onto-voyage [a-cargo & {:keys [voyage-id]}]
  {:pre [(integer? voyage-id)
         (nil? (:voyage-id a-cargo))]}
  (assoc a-cargo :voyage-id voyage-id))

(defn set-cargo-id [a-cargo cargo-id]
  {:pre [(not (:cargo-id a-cargo))]}
  (assoc a-cargo :cargo-id cargo-id))
