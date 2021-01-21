package ch.umb.curo.starter.models

class FlowToNextResult(val nextTasks: List<String> = arrayListOf(),
                       val timeoutExceeded: Boolean = false,
                       val flowToEnd: Boolean = false)

