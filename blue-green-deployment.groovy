/**
 * This script launch a second service (codename blue) to demonstrate blue green deployment
 * (with two services, one blue and one green).
 */

node {
  def globalMethods = load("/var/jenkins_home/pipelines-jenkins/global-methods.groovy")

  stage('Launch v2 service deployment') {
    globalMethods.executeCommandOnPrimaryNode("docker service create -d -p 82:80 --replicas 20 --restart-condition any --limit-memory 20M --name resto-v2 --network " +
        "frontnetwork --with-registry-auth " + globalMethods.SLAVE_REGISTRY_URL + "/" + globalMethods.WEBSITE_IMAGE_NAME + ":" + globalMethods.WEBSITE_OLD_VERSION)
  }
}