package ch.umb.curo.starter.models.response

import java.io.Serializable

class LoginMethod(val id: String,
                  val name: String,
                  val useUsernamePassword: Boolean): Serializable