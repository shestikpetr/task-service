# Creates the kind cluster "cicd-lab4" with a local registry on localhost:5001.
# PowerShell port of the "Local Registry" script from https://kind.sigs.k8s.io/docs/user/local-registry/
$clusterName = 'cicd-lab4'
$regName = 'kind-registry'
$regPort = '5001'

# 1. Registry container (once)
if (-not (docker ps -q -f "name=^$regName$")) {
    docker run -d --restart=always -p "127.0.0.1:${regPort}:5000" --network bridge --name $regName registry:2
}

# 2. Cluster whose containerd looks up registry hosts in /etc/containerd/certs.d
@"
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
containerdConfigPatches:
- |-
  [plugins."io.containerd.grpc.v1.cri".registry]
    config_path = "/etc/containerd/certs.d"
"@ | kind create cluster --name $clusterName --config=-
if ($LASTEXITCODE -ne 0) { throw 'kind create cluster failed' }

# 3. Every node resolves localhost:5001 to the registry container
$regDir = "/etc/containerd/certs.d/localhost:$regPort"
foreach ($node in kind get nodes --name $clusterName) {
    # Written inside the node: piping from Windows PowerShell would prepend a BOM that containerd cannot parse
    docker exec $node sh -c "mkdir -p '$regDir' && printf '[host.\`"http://${regName}:5000\`"]\n' > '$regDir/hosts.toml'"
}

# 4. Registry joins the kind network
if ((docker inspect -f '{{json .NetworkSettings.Networks.kind}}' $regName) -eq 'null') {
    docker network connect kind $regName
}

# 5. Document the local registry for tools in the cluster
@"
apiVersion: v1
kind: ConfigMap
metadata:
  name: local-registry-hosting
  namespace: kube-public
data:
  localRegistryHosting.v1: |
    host: "localhost:$regPort"
    help: "https://kind.sigs.k8s.io/docs/user/local-registry/"
"@ | kubectl apply -f -
