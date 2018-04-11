/**
 * This script launch basic services in order to prepare the demo.
 */

node {

  def globalMethods = load("/var/jenkins_home/pipelines-jenkins/global-methods.groovy")

  stage('Sanitize Services') {
    // Get all services ID.
    def services = globalMethods.executeCommandOnPrimaryNode('docker service ls -q')
    services = services.replaceAll("\\n", " ").replaceAll("\\r", " ")

    // Remove all running services.
    globalMethods.executeCommandOnPrimaryNode('docker service rm ' + services)
  }

  stage('Init Container Network') {
    globalMethods.executeCommandOnPrimaryNode('docker network create --driver overlay frontnetwork')
  }

  globalMethods.launchDockerRegistryService()
  
  stage('Launch LED monitoring service') {
    globalMethods.executeCommandOnPrimaryNode("docker service create -d --name led-manager --mode global --restart-condition any --mount type=bind,src=/sys,dst=/sys " +
        "--mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock --with-registry-auth " + globalMethods.SLAVE_REGISTRY_URL + "/led-manager:latest")
  }
  stage('Launch Portainer Service') {
    globalMethods.executeCommandOnPrimaryNode("docker service create -d --name portainer --publish 9000:9000 --replicas=1 --constraint=node.role==manager --mount " +
        "type=bind,src=//var/run/docker.sock,dst=/var/run/docker.sock portainer/portainer -H unix:///var/run/docker.sock")
  }
  stage('Launch Swarm dashboard service'){
    globalMethods.executeCommandOnPrimaryNode("docker service create -d --name swarm-vizu_1_1_0 --publish=8080:8080/tcp --constraint=node.role==manager --mount=type=bind," +
        "src=/var/run/docker.sock,dst=/var/run/docker.sock --with-registry-auth " + globalMethods.SLAVE_REGISTRY_URL + "/swarm-dashboard:latest")
  }
  stage('Launch website at initial version service'){
    globalMethods.executeCommandOnPrimaryNode("docker service create -d --name resto-v1 -p 81:80 --replicas 16 --restart-condition any --limit-memory 20M --network " +
        "frontnetwork --with-registry-auth " + globalMethods.SLAVE_REGISTRY_URL + "/" + globalMethods.WEBSITE_IMAGE_NAME + ":" + globalMethods.WEBSITE_OLD_VERSION)
  } 
}