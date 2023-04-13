# Azure DevOps Integration

### Next alpha version: 1.13

## Bugs
  * [X] Show threads without thread context too
  * [X] Comment reply UI is broken
  * [O] Reply text area should have padding
  * [?] Why settings are not saved
  * [O] NullPointerException
  * [?] AddNewThread can be opened with shortcuts ?
  * [O] Offset out of bounds

## Plans
  * **1.13 - Improvements**
    * Changed files should have the "reviewed" checkbox
      * Filter out reviewed items
      * Cache the reviewed file flags by **LastChangeOfTheFile => IsReviewed**
    * All bugs fixed


  * **Backlog** 
    * With some themes the bg colors are all over the place
    * If pipeline failed show reason
    * Requeue pipeline
    * Add / Remove reviewer
    * Add / Remove work item
    * Change target branch
    * Cache user icons
    * Cache work item types
    * Transition to services where possible
    * Handle 403 and 400 responses for all requests
    * Changes in files should be cached, and re-downloaded if the version changed
    * Expand all and collapse all for tree view
    * Icons should be based on the theme, not only the original ide icons
    * Create PR line items action (DELETE) should be always visible on the right side
    * Save token as password safe
    * Add errorHandler to plugin
    * Filter for comments and threads
      * Open all
      * Collapse all
    * PR Actions could have better UI
    * When token is expired, have a possibility to quickly enter a new token
    * Send comments with ctrl+enter automatically
    * Support for all devops formatting in for comments
    * Editor with features like in devops
    * After a refresh, refresh only the targeted content and remember if a collapsible is collapsed or open
        * Auto refresh PR every X sec (Settings value)
        * Auto refresh PR threads every X sec (Settings value)
    * Each click on the icon of the commenter in the editor opens a new instance of the ThreadWindow
        * Should be limited to 1 window / thread
    * If a diff view is open, and opens the same diff, do not open another one, just focus the old one
    * Resolve all the TODOS
  