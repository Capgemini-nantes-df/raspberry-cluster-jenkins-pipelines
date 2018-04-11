/**
 * This script download images on a master registry to push it on a local slave registry, installed on the
 * cluster primary node.
 */

node {

    def globalMethods = load("/var/jenkins_home/pipelines-jenkins/global-methods.groovy")

    /**
    * Login to the master registry with credential specified at the top of this file.
    */
    stage('Login to master registry') {
        globalMethods.executeCommandOnPrimaryNode('docker login ' + globalMethods.MASTER_REGISTRY_URL + ' -u ' + globalMethods.MASTER_REGISTRY_LOGIN + ' -p ' + globalMethods.MASTER_REGISTRY_PASSWORD)
    }

    stage('Download images from master registry') {
        globalMethods.REGISTRY_IMAGES.each { key, value ->
            globalMethods.executeCommandOnPrimaryNode('docker pull ' + globalMethods.MASTER_REGISTRY_URL + '/' + value['IMAGE_NAME'])
        }
    } 

    /**
    * Download Docker Registry image for ARM.
    * IMPORTANT : keep this step. Yes, Swarm can download a distant image when a service is launch. But Jenkins don't care
    * about this, and play next step before image finish downloading (and before the service start...).
    */
    stage('Download Docker Registry image for ARM') {
        globalMethods.executeCommandOnPrimaryNode('docker pull ' + globalMethods.DOCKER_REGISTRY_IMAGE_ARM)
    }
    
    globalMethods.launchDockerRegistryService()
    
    /**
    * Tag and push image to local slave registry.
    */
    stage('Tag images and push it to slave registry') {
        globalMethods.REGISTRY_IMAGES.each { key, value ->
            // Tag it with the name of the local (slave) registry.
            globalMethods.executeCommandOnPrimaryNode('docker tag ' + globalMethods.MASTER_REGISTRY_URL + '/' + value['IMAGE_NAME'] + ' ' + globalMethods.SLAVE_REGISTRY_URL + '/' + value['IMAGE_NAME'])

            // Push image on local (slave) registry.
            globalMethods.executeCommandOnPrimaryNode('docker push ' + globalMethods.SLAVE_REGISTRY_URL + '/' + value['IMAGE_NAME'])

            // Remove image with master registry name.
            globalMethods.executeCommandOnPrimaryNode('docker rmi ' + globalMethods.MASTER_REGISTRY_URL + '/' + value['IMAGE_NAME'])
        }
    }
}