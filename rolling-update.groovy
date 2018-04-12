/**
 * This script launch the new website version.
 */

node {

  def globalMethods = load("/var/jenkins_home/pipelines-jenkins/global-methods.groovy")

  stage('Launch Rolling Update Service') {
    globalMethods.executeCommandOnPrimaryNode("docker service update --image " + globalMethods.SLAVE_REGISTRY_URL + "/" + globalMethods.WEBSITE_IMAGE_NAME + ":" + WEBSITE_NEW_VERSION + " --update-delay 0s --update-parallelism 2 --update-failure-action " +
        "rollback --update-order start-first resto")
  }
}