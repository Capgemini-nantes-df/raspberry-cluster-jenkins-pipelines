/**
 * This script init a fresh new swarm cluster.
 */

node {
    def globalMethods = load("/var/jenkins_home/pipelines-jenkins/global-methods.groovy")

    stage("Force nodes to leave Swarm cluster") {
        globalMethods.NODES_DATA.each { key, value ->
            globalMethods.executeCommandOnSpecificNode(globalMethods.getNodeIp(key), 'docker swarm leave -f')
        }
    }

    stage("Create Docker Registry folder") {
        globalMethods.NODES_DATA.each {
            key, value -> if(value['PRIMARY']) {
                globalMethods.executeCommandOnSpecificNode(globalMethods.getNodeIp(key), 'sudo mkdir /mnt/registry')
            }
        }
    }

    stage("Add nodes to swarm cluster") {
        String managerNumber = ''
        globalMethods.NODES_DATA.each {
            key, value -> if(value['PRIMARY']) {

                // Store manager
                managerIp = globalMethods.getNodeIp(key)

                // Init a fresh Swarm cluster.
                globalMethods.executeCommandOnSpecificNode(managerIp, 'docker swarm init')

                // Add a label on this node to deploy Docker Registry.
                globalMethods.executeCommandOnSpecificNode(managerIp, 'docker node update --label-add registry=true ' + value['NAME'])

            } else {
                String token
                if(value['MANAGER-GROUP']) {
                token = globalMethods.retrieveToken(managerIp, "manager")
                } else {
                token = globalMethods.retrieveToken(managerIp, "worker")
                }
                String nodeIp = globalMethods.getNodeIp(key)
                String command = 'docker swarm join --token ' + token.trim() + ' ' + managerIp + ':2377'
                globalMethods.executeCommandOnSpecificNode(nodeIp, command)
            }
        }
    }
}
