(defun notify-send (msg)
  (interactive)
  (shell-command (concat "notify-send '" msg "'")))

(defun cider-interactive-notify-and-eval (code)
  (interactive)
  (notify-send code)
  (cider-interactive-eval
   code
   (cider-interactive-eval-handler nil (point))
   nil
   nil))

(defun notespace/init-with-browser ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   (concat "(notespace.api/init-with-browser)")))

(defun notespace/init ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   (concat "(notespace.api/init)")))

(defun notespace/eval-and-realize-note-at-this-line ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   (concat "(notespace.api/eval-and-realize-note-at-line! "
           (number-to-string (line-number-at-pos))
           ")")))

(defun notespace/eval-this-notespace ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   "(notespace.api/eval-this-notespace!)"))

(defun notespace/render-static-html ()
  (interactive)
  (cider-interactive-notify-and-eval
   "(notespace.api/render-static-html!)"))
