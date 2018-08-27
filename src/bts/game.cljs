(ns bts.game
  (:require
   [reagent.core :as reagent]
   [reagent.cookies :as cookies]
   [reanimated.core :as animation]))
(defn d
  [x]
  (let [_ (js/console.log x)]
    x))

(defn mark-with
  [gs mark]
  (let [to-mark (:to-mark gs)
        marking (first to-mark)]
    (-> gs
        (update :marked conj (merge marking {:mark mark}))
        (assoc :to-mark (rest to-mark)))))


(defn handle-A-button
  [gs]
  (if (:input-blocked gs)
    gs
    (condp = (:screen gs)
           :main-menu (assoc gs :screen :intro-anim)
           :intro-anim (assoc gs :screen :game)
           :game (if (<= (count (:to-mark gs))
                         1)
                   (-> gs
                       (mark-with :pass)
                       (assoc :screen :game-over))
                   (mark-with gs :pass))
           :game-over (assoc gs :screen :main-menu)
      gs)))


(defn handle-B-button
  [gs]
  (condp = (:screen gs)
         :game (if (<= (count (:to-mark gs))
                       1)
                 (-> gs
                     (mark-with :fail)
                     (assoc :screen :game-over))
                 (mark-with gs :fail))
    gs))


(defonce game-state*
  (reagent/atom
   (let []
     {:screen :main-menu
      :high-score (cookies/get :teacher-nightmare-score "none")})))


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
       (reset! bindings?* true))))


(defn main-menu-component
  []
  (let []
    (fn []
      (let [gs @game-state*
            high-score (:high-score gs)]
        [:div {:style {:text-align "center"}}
         [:div {:style {:margin-top 20
                        :font-size 72
                        :font-family "back_to_1982regular"}}
          "Teacher's"]
         [:div {:style {:margin-top 20
                        :font-size 72
                        :font-family "yoster_islandregular"}}
          "Nightmare"]
         [:div {:style {:margin-top 20}}
          "Current High Score: "
          high-score]
         [animation/timeline
          #(swap! game-state* assoc :input-blocked true)
          1000
          #(swap! game-state* assoc :input-blocked false)
          [:div {:style {:margin-top 20}}
           "Press J to start a new game"]]]))))


(defn intro-animation-component
  []
  (let [local-state* (reagent/atom {:s ""})]
    (fn []
      (let [ls @local-state*
            s (:s ls)]
        [:div
         [:div {:style {:font-size 40}}
          "Welcome to the nightmare"]
         [:div {:style {:margin-top 40
                        :font-size 25}}
          s]
         [animation/timeline
          #(swap! local-state* assoc :s "The weekend is over!")
          3000
          #(swap! local-state* assoc :s "You haven't marked the kids' assignments")
          3000
          #(swap! local-state* assoc :s "Going to have to do this quickly...")
          3000
          #(swap! local-state* assoc :s "Press J to give it a pass")
          3000
          #(swap! local-state* assoc :s "Press K to give it a fail")
          3000
          #(swap! local-state* assoc :s "Starting in 3...")
          1000
          #(swap! local-state* assoc :s "Starting in 2...")
          1000
          #(swap! local-state* assoc :s "Starting in 1...")
          1000
          #(swap! game-state* assoc :screen :game)]
         [animation/timeline
          #(swap! game-state* assoc :input-blocked true)
          1000
          #(swap! game-state* assoc :input-blocked false)
          [:div {:style {:margin-top 40}}
           "Press J to skip the tutorial"]]]))))

(defn document-el
  [& children]
  (into [:div {:style {:margin 10
                       :padding 10
                       :border-width 5
                       :border-style "solid"}}]
        children))

(def perf-opts
  ["incredibly weak"
   "incredibly weak"
   "incredibly weak"
   "weak"
   "weak"
   "very weak"
   "very weak"
   "strong"
   "strong"
   "outstanding"])

(def parent-opts
  ["none"
   "violent"
   "involved"
   "involved"
   "average"
   "average"
   "average"
   "average"
   "average"])


(def questions
  [{:q "How many sides does a dodecahedron have?"
    :y "12"
    :n ["11" "13" "at least 10?" "when am I going to use this"]}
   {:q "What's the capital of Argentina?"
    :y "Buenos Aires"
    :n ["Bueno muchas" "huh?" "where?" "Ben 10"]}
   {:q "Who wrote Julius Caesar, Macbeth and Hamlet?"
    :y "Shakespeare"
    :n ["Shakeweight" "Macbeth" "Fred" "what?" "..."]}
   {:q "Who said, 'I think, therefore I am'?"
    :y "Descartes"
    :n ["A horse" "some guy"]}
   {:q "Who wrote the Ugly Duckling?"
    :y "Hans Christian Andersen"
    :n ["some duck" "robin hood" "brothers grimm" "those guys from supernatural"]}
   {:q "What's the capital of Denmark?"
    :y "Copenhagen"
    :n ["cokenbottle"]}
   {:q "Which is the smallest ocean?"
    :y "Arctic"
    :n ["Pacific" "Indian" "ocean" "the one in the north?"]}
   {:q "Are you even reading these questions at this point?"
    :y "False"
    :n ["Hey what the heck?" "What does this mean?" "I'm telling my Dad!" "lol" "lololol"]}
   {:q "What's the fifth planet from the sun"
    :y "Jupiter"
    :n ["You're anus! hahah" "Uranus" "Neptune" "The big one"]}
   {:q "What's the real name of Siddartha Gautama?"
    :y "Buddha"
    :n ["The big guy" "Siddara what?" "When was this in class?"]}
   {:q "How many months have 31 days?"
    :y "7"
    :n ["6" "8" "7.5"]}
   {:q "What horoscope sign has a crab?"
    :y "cancer"
    :n ["the crab one" "i forget" "are you a crab why do you care?"]}
   {:q "What did Joseph Priesley discover in 1774?"
    :y "Oxygen"
    :n ["Oxycotin" "Oxen" "Joseph Pr.. wait who?"]}
   {:q "Who invented television?"
    :y "John Logie Baird"
    :n ["jesus" "einstein" "nawton" "john loogie" "birdman"]}
   {:q "Which Italian leader was terribly afraid of the evil eye?"
    :y "Mussolini"
    :n ["Mustard" "Massoman" "France?" "Mayonaise"]}])


(defn game-component
  []
  (let [time-to-mark 30
        num-to-mark 31
        starting-state
        {:time-left time-to-mark
         :marked '()
         :to-mark (take
                   num-to-mark
                   (repeatedly (fn []
                                 (let [question (rand-nth questions)
                                       right? (rand-nth [true false])]
                                   {:pic-n (rand-int 6)
                                    :perf (rand-nth perf-opts)
                                    :parents (rand-nth parent-opts)
                                    :q-str (:q question)
                                    :a-str (:y question)
                                    :student-right? right?
                                    :student-answer (if right?
                                                      (:y question)
                                                      (rand-nth (:n question)))}))))}]

    (fn []
      (let [gs @game-state*
            time-left (:time-left gs)
            to-mark (:to-mark gs)
            marked (:marked gs)
            marking (first to-mark)
            {:keys [perf parents pic-n q-str a-str student-answer]} marking
            assignments-left (count to-mark)
            dec-time #(swap! game-state* update :time-left dec)]
        [:div
         [:div {:style {:float "right"}}
          "J to pass K to fail"]
         [:div {:style {:display "flex"}}
          [:img {:src "img/paperstack.png"
                 :style {:margin-right 5}}]
          [:div
           (str assignments-left " assignments to mark")]]
         [:div {:style {:margin-top 20}}
          (str "Time left: " time-left)]
         [:div {:style {:margin-top 20
                        :display "flex"}}
          (document-el
           [:div {:style {:font-size 20}}
            "submitted assignment"]
           [:div {:style {:margin-top 15}}
            (str student-answer)])
          (document-el
           [:div {:style {:font-size 20}}
            "Marking Key"]
           [:div {:style {:margin-top 15}}
            (str "Q: " q-str)]
           [:div {:style {:margin-top 15}}
            (str "A: " a-str)])
          (document-el
           [:div {:style {:font-size 20}}
            "student details"]
           [:div {:style {:margin-top 10
                          :display "flex"
                          :justify-content "center"}}
            [:div {:style {:border "2px solid"
                           :padding 2}}
             (when pic-n
               [:img {:src (str "img/student" pic-n ".png")}])]]
           [:div {:style {:margin-top 10}}
            "past performance:"]
           [:div {:style {:margin-left 10}}
            perf]
           [:div {:style {:margin-top 10}}
            "parents:"]
           [:div {:style {:margin-left 10}}
            parents])]
         [animation/timeline
          #(swap! game-state* conj starting-state)]
         (-> [animation/timeline]
             (into (take (* 2 time-to-mark) (cycle [1000 dec-time])))
             (conj #(swap! game-state* assoc :screen :game-over)))]))))


(defn marked-correctly?
  [assignment]
  (let [student-right? (:student-right? assignment)
        mark (:mark assignment)]
    (if mark
      (or (and (= mark :pass)
               student-right?)
          (and (= mark :fail)
               (not student-right?)))
      false)))

(defn game-over-assignment
  [assignment]
  (let [correct-mark? (marked-correctly? assignment)]
    [:div {:style {:border (if correct-mark?
                             "5px solid green"
                             "5px solid black")
                   :margin 5
                   :padding 5}}]))


(defn game-over-component
  []
  ;; we can calculate this outside the render fn as it won't change
  (let [prev-gs @game-state*
        marked (:marked prev-gs)
        num-marked (count marked)
        unmarked (:to-mark prev-gs)
        num-unmarked (count unmarked)
        num-correct (count (filter marked-correctly? marked))
        num-incorrect (- num-marked num-correct)
        points (reduce
                (fn [accum {:keys [parents mark] :as assignment}]
                  (let [correct-mark? (marked-correctly? assignment)
                        violence? (and (not correct-mark?) (= "violent" parents))
                        complaint? (and (not correct-mark?) (= "involved" parents))
                        failed-orph? (and (= :fail mark) (= "none" parents))]
                    (cond-> accum
                      (and mark (not correct-mark?)) (update :incorrect-marks inc)
                      violence? (update :violence inc)
                      complaint? (update :complaints inc)
                      failed-orph? (update :failed-orphans inc))))
                {:incorrect-marks 0
                 :violence 0
                 :complaints 0
                 :failed-orphans 0}
                (let [x (concat marked unmarked)]
                  x))
        {:keys [violence complaints failed-orphans incorrect-marks]} points
        incorrect-mark-points (* -100 incorrect-marks)
        not-marked-points (* -50 num-unmarked)
        violence-points (* -600 violence)
        complaint-points (* -200 complaints)
        failed-orphan-points (* -450 failed-orphans)
        total-points (+ violence-points complaint-points failed-orphan-points)
        high-score (:high-score prev-gs)
        _ (if (or (= high-score "none") (> total-points high-score))
            (do
              (swap! game-state* assoc :high-score total-points)
              (cookies/set! :teacher-nightmare-score total-points)))]
    (fn []
      (let []
        [:div {:style {:text-align "center"}}
         [:div {:style {:font-size 60}}
          "Game over!"]
         [:div
          (when (> num-marked 0)
            [:div {:style {:margin-top 15}}
             "Marked assignments:"
             (into [:div {:style {:display "flex"
                                  :flex-wrap "wrap"}}]
                   (map game-over-assignment marked))])
          (when (> num-unmarked 0)
            [:div {:style {:margin-top 15}}
             "Unmarked assignments:"
             (into [:div {:style {:display "flex"
                                  :flex-wrap "wrap"}}]
                   (map game-over-assignment unmarked))])]
         [:table {:style {:margin-top 15}}
          [:tbody
           [:tr
            [:td
             (str num-correct " marked correctly")]
            [:td
             "No points, it's your job"]]
           [:tr
            [:td
             (str incorrect-marks " marked incorrectly")]
            [:td
             incorrect-mark-points]]
           [:tr
            [:td
             (str num-unmarked " not marked")]
            [:td
             not-marked-points]]
           [:tr
            [:td
             (str complaints " parent complaints to admin")]
            [:td
             complaint-points]]
           [:tr
            [:td
             (str violence " angry parents came to school")]
            [:td
             violence-points]]
           [:tr
            [:td
             (str failed-orphans " failed orphans")]
            [:td
             failed-orphan-points]]
           [:tr
            [:td {:style {:padding-top 30
                          :font-weight 600}}
             "Total points"]
            [:td {:style {:padding-top 30
                          :font-weight 600}}
             total-points]]]]
         [animation/timeline
          #(swap! game-state* assoc :input-blocked true)
          1000
          #(swap! game-state* assoc :input-blocked false)
          [:div {:style {:margin-top 20}}
           "Press J to go to the main menu"]]]))))


(defn app-component
  []
  (fn []
    (let [game-state @game-state*
          screen (:screen game-state)]
      [:div {:style {:border-style "double"
                     :border-width 12
                     :padding 20}}
       (condp = screen
         :main-menu [main-menu-component]
         :intro-anim [intro-animation-component]
         :game [game-component]
         :game-over [game-over-component])])))
