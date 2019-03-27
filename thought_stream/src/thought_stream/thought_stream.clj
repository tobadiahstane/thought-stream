(ns thought-stream.thought-stream)

(defrecord StartThinking [id email])

(defrecord ThinkingStarted [id email])

(defrecord HaveThought [id thought link connecting focus])

(defrecord Thought [id thought link connecting focus])

(defrecord ThinkingThoughts [id])

(defprotocol ThoughtStream
  (start-thinking-thoughts [thinking])
  (have-thought [thinking thought])
  (connect-thought [thinking thought connecting]))

(defprotocol ExecuteCommand
  (execute [command]))

(defprotocol StoreEvents
  (store [aggregate-with-changes]))

(defn thinking
  "Command to begin thinking.
  Outputs a Start event."
  [new-thinking]
  (map->StartThinking new-thinking))

(defn having
  "Command to begin having a new thought.
  Outputs a HaveThought command."
  [new-thought]
   (map->HaveThought new-thought))


(defn start [command-fn]
  (fn [request-map]
    (execute (command-fn request-map))))


