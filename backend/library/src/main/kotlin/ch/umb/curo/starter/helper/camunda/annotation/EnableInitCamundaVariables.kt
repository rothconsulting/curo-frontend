package ch.umb.curo.starter.helper.camunda.annotation

/**
 * Enables Curo automatic variable initialization for this class during process start.
 * Curo will automatically attach a generic start listener to the process to trigger `CamundaVariableHelper.initVariables()` on process start.
 */
@Target(AnnotationTarget.CLASS)
annotation class EnableInitCamundaVariables(
    /**
     * Define related process definition keys.
     * If empty this class will be used for all processes
     */
    val value: Array<String> = []
)

