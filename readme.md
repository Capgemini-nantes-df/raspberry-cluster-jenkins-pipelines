# Repository goal

This repository store Jenkins pipelines used to introduce DevOps concepts with multiples cases. We use a cluster of 5 Raspberry Pi for this demo.

## Cases

### Build Docker Swarm cluster

*File : swarm-initialisation.groovy*

This pipeline make sure each cluster node is not a part of a Swarm cluster, and then initiate a new cluster. This cluster has 2 managers (node 1 and 2) and 3 workers (node 3 to 5).

### Create a local slave Docker Registry

*File : setup-registry.groovy*

This pipeline start a docker registry service on the cluster primary node, download docker images from master registry (configured in global-methods.groovy), tag it and push it to local slave registry.

### Setup the demo

*File : prepare-demo.groovy*

This task will start all the services needed to run the demo :

- LED Manager,
- Portainer,
- A Swarm cluster dashboard,
- Initial website.

### Rollback

*File : rollback.groovy*

This pipeline start a docker service design to failed at launch. This allow us to demonstre Docker Swarm rollback capabilities.

### Rolling update

*File : rolling-update.groovy*

This pipeline update current website.

### Blue Green deployment

*File : blue-green-deployment.groovy*

This pipeline run old and current website version.