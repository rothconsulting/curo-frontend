package ch.umb.curo.starter.property

class CuroUserFederationProperties {

    /**
     * Defines if Curo user federation is active
     */
    var enabled: Boolean = false

    /**
     * Defines if Curo create non existing users in Camunda
     */
    var createNonExistingUsers: Boolean = true

    /**
     * Claim which is used for firstname
     */
    var firstNameClaim = "given_name"

    /**
     * Claim which is used for lastname
     */
    var lastNameClaim = "family_name"

    /**
     * Claim which is used for email
     */
    var emailClaim = "email"

    /**
     * Defines if Curo map roles to groups. If not, Curo will use the groupClaim to load a list of groups
     */
    var loadGroupFromRoles = true

    /**
     * Claim which is used for groups if loadGroupFromRoles is disabled
     */
    var groupClaim = "groups"

    /**
     * Claim which is used for resource access
     */
    var resourceAccessClaim = "resource_access"

    /**
     * Defines the resource from the the roles are used if loadGroupFromRoles is enabled
     */
    var resourceName = ""

    /**
     * Defines if Curo should log non existing jwt groups
     */
    var printNonExistingGroups = false

    /**
     * Don't revoke the camunda-admin group from users even if they don't have this group in their JWT
     */
    var dontRevokeCamundaAdminGroup = false

}
