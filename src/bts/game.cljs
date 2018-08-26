(ns bts.game
  (:require
   [reagent.core :as reagent]
   [reanimated.core :as animation]))

(defn mark-should-be
  [assignment]
  (if (even? (:n assignment)) :pass :fail))


(defn marked-correctly?
  [assignment]
  (= (mark-should-be assignment) (:mark assignment)))



(defn mark-with
  [gs mark]
  (let [to-mark (:to-mark gs)
        marking (first to-mark)]
    (-> gs
        (update :marked conj (merge marking {:mark mark}))
        (assoc :to-mark (rest to-mark)))))


(defn handle-A-button
  [gs]
  (js/console.log "A")
  (if (:input-blocked gs)
    gs
    (condp = (:screen gs)
           :main-menu (assoc gs :screen :intro-anim)
           :intro-anim (assoc gs :screen :game)
           :game (mark-with gs :pass)
           :game-over (assoc gs :screen :main-menu)
      gs)))


(defn handle-B-button
  [gs]
  (js/console.log "B")
  (condp = (:screen gs)
         :game (mark-with gs :fail)
    gs))


(defn handle-both-buttons
  [gs]
  (js/console.log "A & B")
  gs)


(defonce game-state*
  (reagent/atom
   (let []
     {:screen :main-menu
      :high-score 0})))


;; defonce didn't seem to work here? re-bound keys on file save
(defonce bindings?* (atom false))
(defonce bind-keypress-listener
   (if-not @bindings?*
     (do
       (js/addEventListener
        "keydown"
        (fn [e]
          (let [key (.-key e)]
            (condp = key
              "j" (swap! game-state* handle-A-button)
              "k" (swap! game-state* handle-B-button)
              nil))))
       (js/console.log "==== keys are now bound ====")
       (reset! bindings?* true))))


(defn main-menu-component
  []
  (let []
    (fn []
      (let [gs @game-state*
            high-score (:high-score gs)]
        [:div]
        [:div
         [:div
          "Back to School: Teacher's Nightmare"]
         [:div
          "Current High Score: "
          high-score]
         [animation/timeline
          #(swap! game-state* assoc :input-blocked true)
          1000
          #(swap! game-state* assoc :input-blocked false)
          [:div
           "Press J to start a new game"]]]))))


(defn intro-animation-component
  []
  (let [local-state* (reagent/atom {:s "Hello"})]
    (fn []
      (let [ls @local-state*
            s (:s ls)]
        [:div
         [:span {:style {:font-size "10rem"}}
          s]
         [animation/timeline
          #(swap! game-state* assoc :input-blocked true)
          1000
          #(swap! game-state* assoc :input-blocked false)
          [:div
           "Press J to skip the intro"]]
         [animation/timeline
          1000
          #(swap! local-state* assoc :s "3")
          1000
          #(swap! local-state* assoc :s "2")
          1000
          #(swap! local-state* assoc :s "1")
          1000
          #(swap! game-state* assoc :screen :game)]]))))


(defn game-component
  []
  (let [time-to-mark 10
        num-to-mark 30
        starting-state {:time-left time-to-mark
                        :to-mark (take
                                  num-to-mark
                                  (repeatedly (fn []
                                                {:n (rand-int 10)})))
                        :marked '()}]
    (fn []
      (let [gs @game-state*
            time-left (:time-left gs)
            to-mark (:to-mark gs)
            marking (first to-mark)
            marking-n (:n  marking)
            assignments-left (count to-mark)
            dec-time #(swap! game-state* update :time-left dec)]
        [:div
         [:div
          (str assignments-left " assignments to mark")]
         [:div
          (str "Time left: " time-left)]
         [:div
          (str "Next n: " marking-n)]
         [animation/timeline
          #(swap! game-state* conj starting-state)]
         (-> [animation/timeline]
             (into (take (* 2 time-to-mark) (cycle [1000 dec-time])))
             (conj #(swap! game-state* assoc :screen :game-over)))]))))

(defn game-over-assignment
  [assignment]
  (let [mark (:mark assignment)
        correct (marked-correctly? assignment)]
    [:div {:style {:border (if correct
                             "5px solid green"
                             "5px solid black")
                   :margin 5
                   :padding 5}}
     (when mark [:div
                 (str "You: " mark)])
     [:div
      (str "Actual: " (mark-should-be assignment))]]))


(defn game-over-component
  []
  (let []
    (fn []
      (let [gs @game-state*
            marked (:marked gs)
            num-marked (count marked)
            unmarked (:to-mark gs)
            num-correct (count (filter marked-correctly? marked))
            num-incorrect (- num-marked num-correct)]
        [:div
         [:div
          "Game over!"]
         [:div
          (str
           "You marked "
            num-correct
           "/"
           (+ num-correct num-incorrect)
           " assignments correctly")]
         [:div
          [:div
           "Marked assignments:"]
          (into [:div {:style {:display "flex"
                               :flex-wrap "wrap"}}]
                (map game-over-assignment marked))
          [:div
           "Unmarked assignments:"]
          (into [:div {:style {:display "flex"
                               :flex-wrap "wrap"}}]
                (map game-over-assignment unmarked))]
         [animation/timeline
          #(swap! game-state* assoc :input-blocked true)
          1000
          #(swap! game-state* assoc :input-blocked false)
          [:div
           "Press J to go to the main menu"]]]))))


(defn app-component
  []
  (fn []
    (let [game-state @game-state*
          screen (:screen game-state)]
      [:div {:style {:border "2px dashed"
                     :padding 20}}
       (condp = screen
         :main-menu [main-menu-component]
         :intro-anim [intro-animation-component]
         :game [game-component]
         :game-over [game-over-component])])))
