(ns bts.core
  (:require
   [reagent.core :as reagent]
   [bts.game :as game]))


(reagent/render [game/app-component]
                (.-body js/document))
