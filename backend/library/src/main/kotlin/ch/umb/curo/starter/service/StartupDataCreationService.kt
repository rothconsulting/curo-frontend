package ch.umb.curo.starter.service

import org.camunda.bpm.engine.ProcessEngine

interface StartupDataCreationService {

    fun createInitialGroups(engine: ProcessEngine)
    fun createInitialUsers(engine: ProcessEngine)

}
