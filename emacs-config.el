;; Emacs config for Notespace v3

(defun cider-interactive-notify-and-eval (code)
  (interactive)
  (message code)
  (cider-interactive-eval
   code
   (cider-interactive-eval-handler nil (point))
   nil
   nil))

(defun notespace/eval-and-realize-note-at-this-line ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   (concat "(notespace.api/eval-and-realize-note-at-line "
           (number-to-string (line-number-at-pos))
           ")")))

(defun notespace/eval-and-realize-notes-from-this-line ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   (concat "(notespace.api/eval-and-realize-notes-from-line "
           (number-to-string (line-number-at-pos))
           ")")))

(defun notespace/eval-and-realize-notes-from-change ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   (concat "(notespace.api/eval-and-realize-notes-from-change)")))

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

(defun notespace/eval-this-notespace ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   "(notespace.api/eval-this-notespace)"))

(defun notespace/eval-and-realize-this-notespace ()
  (interactive)
  (save-buffer)
  (cider-interactive-notify-and-eval
   "(notespace.api/eval-and-realize-this-notespace)"))

(defun notespace/render-static-html ()
  (interactive)
  (cider-interactive-notify-and-eval
   "(notespace.api/render-static-html)"))

;; suggested emacs key binding (thanks @mchampine)
(add-hook 'clojure-mode-hook
          (lambda ()
            (define-key clojure-mode-map (kbd "C-c n e") 'notespace/eval-this-notespace)
            (define-key clojure-mode-map (kbd "C-c n r") 'notespace/eval-and-realize-this-notespace)
            (define-key clojure-mode-map (kbd "C-c n n") 'notespace/eval-and-realize-note-at-this-line)
            (define-key clojure-mode-map (kbd "C-c n f") 'notespace/eval-and-realize-notes-from-this-line)
            (define-key clojure-mode-map (kbd "C-c n i b") 'notespace/init-with-browser)
            (define-key clojure-mode-map (kbd "C-c n i i") 'notespace/init)
            (define-key clojure-mode-map (kbd "C-c n s") 'notespace/render-static-html)
            (define-key clojure-mode-map (kbd "C-c n c") 'notespace/eval-and-realize-notes-from-change)))

;; suggested spacemacs key bindings:
(spacemacs/set-leader-keys-for-major-mode 'clojure-mode
  "n e" 'notespace/eval-this-notespace
  "n r" 'notespace/eval-and-realize-this-notespace
  "n n" 'notespace/eval-and-realize-note-at-this-line
  "n f" 'notespace/eval-and-realize-notes-from-this-line
  "n i b" 'notespace/init-with-browser
  "n i i" 'notespace/init
  "n s" 'notespace/render-static-html
  "n c" 'notespace/eval-and-realize-notes-from-change)

