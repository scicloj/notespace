(ns notespace.v1.js
  (:require [notespace.v1.config :as config]))

(defn mirador-setup []
  (->> (:live-reload-port @config/defaults)
       (format
        "<script language=\"JavaScript\">
  socket= new WebSocket('ws://localhost:%s/watch-reload');
  socket.onopen= function() {
                           socket.send ('watch') ;
                           };
  socket.onmessage= function(s) {
                               if                                                          ( s.data == 'started') {
                                                                                                                   console.log (\"Watching started\") ;
                                                                                                                   }                                 else                   if ( s.data == 'reload') {
                                                                                                                                                                                                      console.log            (\"reloading\") ;
                                                                                                                                                                                                      window.location.reload ()            ;
                                                                                                                                                                                                      } else                   {
                                                                                                                                                                                                                                alert('Don\\'t know what to do with [' + s.data + ']');
                                                                                                                                                                                                                                }
                               };
  </script>
  ")))
