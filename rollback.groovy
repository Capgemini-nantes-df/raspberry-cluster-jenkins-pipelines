/**
 * This script launch a service design to failed in order to demonstrate automatic rollback.
 */

node {

  def globalMethods = load("/var/jenkins_home/pipelines-jenkins/global-methods.groovy")

  stage('Try to launch website update') {
    globalMethods.executeCommandOnPrimaryNode("docker service update --image " + globalMethods.SLAVE_REGISTRY_URL + "/" + globalMethods.WEBSITE_IMAGE_NAME + ":" + WEBSITE_CORRUPTED_VERSION + " --update-delay 0s --update-parallelism 2 --update-failure-action " +
        "rollback --update-order start-first --rollback-order start-first --rollback-failure-action pause --rollback-delay 0s " +
        "--rollback-parallelism 2 --rollback-monitor 0s resto-v1")
  }

  // Force this job to fail.
  stage('Check if deployment OK') {
    sh 'exit 1'
  }
}