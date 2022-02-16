FROM debian:stable-slim
HEALTHCHECK --start-period=30s CMD curl --fail http://localhost:8080 || exit 1
VOLUME /tmp/boat
ENV JAVA_HOME="./jdk"
RUN apt-get update && apt-get install -y curl wget unzip dnsutils \
&& curl https://raw.githubusercontent.com/sormuras/bach/master/install-jdk.sh -o install-jdk.sh && chmod +x install-jdk.sh;./install-jdk.sh -v -f 17 --target ./jdk \
&& rm -rf jdk.tar.gz && rm -rf ./jdk/lib/src.zip \
&& ./jdk/bin/jlink --compress=2 --no-header-files --no-man-pages --vm=server --module-path ./jdk/jmods --add-modules ALL-MODULE-PATH --output=./jre \
&& rm -rf ./jdk/ \
#&& rm -rf /tmp/*
ENV PATH="${PATH}:./jre/bin"
#USER nobody
# copy config for boat & if used rlcone
COPY .env .env
EXPOSE 8080
CMD wget https://github.com/rarspace01/cryptowatcher/releases/latest/download/cryptowatcher.jar -O cryptowatcher.jar && chmod +x cryptowatcher.jar && ./jre/bin/java -XX:+HeapDumpOnOutOfMemoryError -jar cryptowatcher.jar