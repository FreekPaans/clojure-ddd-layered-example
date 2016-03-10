(ns layered-example.data.mysql-cargo-repository
  (:refer-clojure :exclude [find])
  (:require [layered-example.domain.cargo.cargo-repository :refer [CargoRepository -find -add! -update!]]
            [layered-example.domain.cargo.cargo :refer [map->Cargo set-cargo-id]]
            [clojure.set :refer [rename-keys]]
            [clojure.java.jdbc :as db]))

(defn row->cargo [row]
  (-> row
      (select-keys [:id :size :voyage_id])
      (rename-keys {:id :cargo-id :voyage_id :voyage-id})
      map->Cargo))
  
(defn cargo->row [cargo version]
  (-> cargo
      (select-keys [:size :voyage-id])
      (rename-keys {:voyage-id :voyage_id})
      (assoc :version version)))

(defn find [mysql-config cargo-id]
  (let [row (-> (db/query mysql-config ["select * from cargoes where id=?" cargo-id])
                first)]
    (when row
      {:version (:version row) :cargo (row->cargo row)})))

(defn add! [mysql-config cargo]
  {:pre [(nil? (:cargo-id cargo))]}
  (let [cargo-id (-> (db/insert! mysql-config :cargoes (cargo->row cargo 1))
                     first
                     :generated_key)]
    (set-cargo-id cargo cargo-id)))

(defn update! [mysql-config version {:keys [cargo-id] :as cargo}]
  {:pre [cargo-id]}
  (db/update! mysql-config :cargoes (cargo->row cargo (inc version)) ["id=?" cargo-id])
  nil)

(defn new-cargo-repository [mysql-config]
  (reify CargoRepository
    (-find [this cargo-id] (find mysql-config cargo-id))
    (-add! [this cargo] (add! mysql-config cargo))
    (-update! [this concurrency-version cargo] (update! mysql-config concurrency-version cargo))))
