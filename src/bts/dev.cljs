(ns bts.dev
  (:require-macros
   [devcards.core :refer [defcard defcard-rg start-devcard-ui!]])
  (:require
   [bts.game :as game]
   [devcards.core]))


(defcard-rg app
  [game/app-component]
  {}
  {:frame false})


(start-devcard-ui!)
