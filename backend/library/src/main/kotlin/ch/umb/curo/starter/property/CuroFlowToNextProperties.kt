package ch.umb.curo.starter.property

class CuroFlowToNextProperties {
    /**
     * Default timeout in seconds for flowToNext requests
     * Default: 30s
     */
    var defaultTimeout: Int = 30

    /**
     * How long in milliseconds should Curo wait between requests to Camunda if flowToNext got requested
     * Default: 500ms
     */
    var interval: Int = 500

    /**
     * Should Curo ignore the assignee for flowToNext requests
     * Default: false
     */
    var ignoreAssignee: Boolean = false

}
