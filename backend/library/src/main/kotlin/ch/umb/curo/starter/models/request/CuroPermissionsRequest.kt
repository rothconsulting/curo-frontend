package ch.umb.curo.starter.models.request

import org.camunda.bpm.engine.authorization.Resources

class CuroPermissionsRequest: HashMap<String, HashMap<Resources, List<String>>>()
