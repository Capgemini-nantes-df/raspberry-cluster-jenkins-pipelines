/**
 * This script defines globals methods and environment vars used by pipelines 
 */

// Cluster variables.
RPI_USER = 'pirate' // User used to connect on each Raspberry.
DHCP_POOL = '192.168.0.' // Cluster DHCP pool as configured in your router.
MASTER_NODE_IP = '192.168.0.101'

// Registry parameters.
MASTER_REGISTRY_URL = 'VALUE' // Master registry URL. Don't add protocol (http:// || https://).
MASTER_REGISTRY_LOGIN = 'VALUE'
MASTER_REGISTRY_PASSWORD = 'VALUE'
SLAVE_REGISTRY_URL = '192.168.0.101:80'

// Images parameters.
DOCKER_REGISTRY_IMAGE_ARM = 'armbuild/registry:2'
WEBSITE_IMAGE_NAME = 'resto'
// Tag for the "WEBSITE_IMAGE_NAME". (ie :  [WEBSITE_IMAGE_NAME]:[WEBSITE_OLD_VERSION])
WEBSITE_OLD_VERSION = 'blue'
WEBSITE_NEW_VERSION = 'green'
WEBSITE_CORRUPTED_VERSION = 'orange' // Special version designed to fail when service start.

/**
  * Nodes list.
  */
NODES_DATA = [
    '101' : ['MANAGER-GROUP': true, 'PRIMARY': true,  'NAME': 'node-1'],
    '102' : ['MANAGER-GROUP': true, 'PRIMARY': false, 'NAME': 'node-2'],
    '103' : ['MANAGER-GROUP': false, 'PRIMARY': false, 'NAME': 'node-3'],
    '104' : ['MANAGER-GROUP': false, 'PRIMARY': false, 'NAME': 'node-4'],
    '105' : ['MANAGER-GROUP': false, 'PRIMARY': false, 'NAME': 'node-5']
]

/**
  * List of images used.
  */
REGISTRY_IMAGES = [
    '1' : ['IMAGE_NAME': WEBSITE_IMAGE_NAME + ':' + WEBSITE_OLD_VERSION],
    '2' : ['IMAGE_NAME': WEBSITE_IMAGE_NAME + ':' + WEBSITE_NEW_VERSION],
    '3' : ['IMAGE_NAME': WEBSITE_IMAGE_NAME + ':' + WEBSITE_CORRUPTED_VERSION],
    '4' : ['IMAGE_NAME': 'led-manager:latest'],
    '5' : ['IMAGE_NAME': 'swarm-dashboard:latest']
]

/**
  * Retrieve Docker Swarm token for managers and workers.
  */
def retrieveToken(String managerIp, String nodeType) {
  return executeCommandOnSpecificNode(managerIp, 'docker swarm join-token ' + nodeType + ' -q')
}

/**
  * Build node ip address with DHCP pool and node number.
  */
def getNodeIp(String nodeNumber) {
  return DHCP_POOL + nodeNumber
}

/**
  * Execute SSH command on a specific node node.
  */
def executeCommandOnSpecificNode(String host, String command) {
  try {
    sh (returnStdout: true, script: 'ssh -i /usr/share/jenkins/.ssh/id_rsa ' + RPI_USER + '@' + MASTER_NODE_IP + ' ' + 'ssh ' + RPI_USER + '@' + host + ' ' + command)
  } catch(Exception e) {
    echo 'Error : trying to execute command "' + command + '". Exception : "' + e + '")'
  }
}

/**
  * Execute SSH command on primary node.
  */
def executeCommandOnPrimaryNode(String command) {
  try {
    sh (returnStdout: true, script: 'ssh -i /usr/share/jenkins/.ssh/id_rsa ' + RPI_USER + '@' + MASTER_NODE_IP + ' ' + command)
  } catch(Exception e) {
    echo 'Error. Trying to execute command ' + command + '(Exception : ' + e + ')'
  }
}

/**
  * Launch a local Docker Registry on primary node.
  */
def launchDockerRegistryService(){
  stage('Launch slave registry') {
      executeCommandOnPrimaryNode("docker service create -d --name registry -p 80:80 --replicas 1 --constraint=node.labels.registry==true --mount type=bind,src=/mnt/registry,dst=/var/lib/registry -e REGISTRY_HTTP_ADDR=0.0.0.0:80 --detach=true " + DOCKER_REGISTRY_IMAGE_ARM)
  }
}

return this