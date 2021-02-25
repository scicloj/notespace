(ns notespace.cljfx.api
   "Main API namespace for cljfx -- a partial version, extracted for internal use in Notespace
  
  Requiring this namespace starts JavaFX runtime if it wasn't previously started

  Sections:
 - context:
    - [[create-context]] - wrap map in a black box that memoizes function subscriptions to
      it
    - [[sub-ctx]] and [[sub-val]] - extract value from a context and memoize it
    - [[swap-context]] - create new context using function that reuses existing cache
    - [[reset-context]] - derive new context that reuses existing cache
    - [[unbind-context]] - debug utility that releases context from dependency tracking
  - event handling:
    - [[wrap-co-effects]] - wrap event handler to replace input side effects (such as
      derefing app state) with pure functions
    - [[make-deref-co-effect]] - helper function that creates deref co-effect
    - [[wrap-effects]] - wrap event handler to replace output side effects (such as
      updating app state, performing http requests etc.) with pure functions
    - [[make-reset-effect]] - helper function that creates effect that resets atom
    - [[dispatch-effect]] - effect that allows dispatching another events"
   (:require [notespace.cljfx.context :as context]
             [notespace.cljfx.event-handler :as event-handler]))

(defn create-context
  "Create a memoizing context for a value

  Context should be treated as a black box with [[sub-val]]/[[sub-ctx]] as an interface
  to access context's content.

  [[sub-val]] subscribes to a function that receives current value in the context,
  should be fast like [[get]].
  [[sub-ctx]] subscribes to a function that receives context to subscribe to other
  functions, can be slow like [[sort]]

  Values returned by `sub-*` will be memoized in this context, resulting in cache lookups
  for subsequent `sub-*` calls on corresponding functions with same arguments.

  Cache will be reused on contexts derived by `swap-context` and `reset-context`
  to minimize recalculations. To make it efficient, all calls to `sub-*` by subscription
  functions are tracked, thus calling `sub-*` from subscription function is not allowed
  after that function returns. For example, all lazy sequences that may
  call `sub-*` during computing of their elements have to be realized."
  ([m]
   (create-context m identity))
  ([m cache-factory]
   (context/create m cache-factory)))

(defn sub-val
  "Subscribe to a function that receives value in this context

  This creates a direct subscription that will be recalculated whenever the context
  changes.

  Should be fast as [[get]]"
  [context f & args]
  (apply context/sub-val context f args))

(defn sub-ctx
  "Subscribe to a function that receives the context

  This is used for creating indirect subscriptions by calling [[sub-ctx]]/[[sub-val]]
  inside the function that will be recalculated only when those subscriptions change.

  Can be slow as [[sort]]"
  [context f & args]
  (apply context/sub-ctx context f args))

(defn swap-context
  "Create new context with context map being (apply f current-map args), reusing existing
  cache"
  [context f & args]
  (apply context/swap context f args))

(defn reset-context
  "Create new context with context map being m, reusing existing cache"
  [context m]
  (context/reset context m))

(defn unbind-context
  "Frees context from tracking subscription functions

  During debugging it may be useful to save context from subscription function to some
  temporary state and then explore it. In that case context should be freed from tracking
  debugged subscription function by calling `unbind-context` on it"
  [context]
  (context/unbind context))

(defn wrap-co-effects
  "Event handler wrapper intended to provide mutable external dependencies as immutable
  values to make event handler pure. Transforms `f` of 2 args (dependency map + event)
  to function of 1 argument (event)

  `co-effect-id->producer` is a map from arbitrary keys to zero-argument side-effecting
  functions, which are used to produce a dependency map"
  [f co-effect-id->producer]
  (event-handler/wrap-co-effects f co-effect-id->producer))

(defn make-deref-co-effect
  "Creates co-effect function that derefs a `*ref` when it's realized"
  [*ref]
  (event-handler/make-deref-co-effect *ref))

(defn wrap-effects
  "Event handler wrapper intended to execute side effects described by otherwise pure `f`

  `f` is a function of 1 argument (event) that returns data describing possible side
  effects: a series of 2-element vectors, where 1st value corresponds to key of
  side-effecting consumer in `effect-id->consumer`, and 2nd is an argument to that
  consumer

  `effect-id->consumer` is a map from arbitrary keys to 2-argument side-effecting
  functions. 1st argument is a value provided by `f`, and second is a 1-arg event
  dispatcher that can be called with new events and will eventually call `f`

  Returns function that takes event (and optionally custom dispatcher function) and
  executes side effects described by returned values of `f`"
  [f effect-id->consumer]
  (event-handler/wrap-effects f effect-id->consumer))

(defn make-reset-effect
  "Creates effect function that reset an `*atom` when this effect is triggered"
  [*atom]
  (event-handler/make-reset-effect *atom))

(def dispatch-effect
  "Effect function that dispatches another event when this effect is triggered"
  event-handler/dispatch-effect)

