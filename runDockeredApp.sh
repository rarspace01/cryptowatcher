echo $CR_PAT | docker login ghcr.io -u rarspace01 --password-stdin
docker run --env-file ./env --restart always -d -it -p 8080:8080 ghcr.io/rarspace01/cryptowatcher