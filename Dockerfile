FROM adoptopenjdk:11-jre-hotspot
ARG VERSION

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

LABEL "maintainer"="DITA Open Toolkit project"
LABEL "org.opencontainers.image.authors"="https://www.dita-ot.org/who_we_are"
LABEL "org.opencontainers.image.documentation"="https://www.dita-ot.org/"
LABEL "org.opencontainers.image.vendor"="DITA Open Toolkit project"
LABEL "org.opencontainers.image.licenses"="Apache-2.0"
LABEL "org.opencontainers.image.title"="DITA Open Toolkit"
LABEL "org.opencontainers.image.description"="Publishing engine for content authored in the Darwin Information Typing Architecture."
LABEL "org.opencontainers.image.source"="https://github.com/dita-ot/dita-ot"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update -q && \
    apt-get install -qy --no-install-recommends -y unzip locales tzdata && \
    rm -rf /var/lib/apt/lists/*

RUN curl -sLo /tmp/dita-ot-$VERSION.zip https://github.com/dita-ot/dita-ot/releases/download/$VERSION/dita-ot-$VERSION.zip && \
    unzip -qq /tmp/dita-ot-$VERSION.zip -d /tmp/ && \
    rm /tmp/dita-ot-$VERSION.zip && \
    mkdir -p /opt/app/ && \
    mv /tmp/dita-ot-$VERSION/bin /opt/app/bin && \
    chmod 755 /opt/app/bin/dita && \
    mv /tmp/dita-ot-$VERSION/config /opt/app/config && \
    mv /tmp/dita-ot-$VERSION/lib /opt/app/lib && \
    mv /tmp/dita-ot-$VERSION/plugins /opt/app/plugins && \
    mv /tmp/dita-ot-$VERSION/build.xml /opt/app/build.xml && \
    mv /tmp/dita-ot-$VERSION/integrator.xml /opt/app/integrator.xml && \
    rm -r /tmp/dita-ot-$VERSION && \
    /opt/app/bin/dita --install

RUN useradd -ms /bin/bash dita-ot && \
    chown -R dita-ot:dita-ot /opt/app
USER dita-ot

ENV DITA_HOME=/opt/app
ENV PATH=${PATH}:${DITA_HOME}/bin
WORKDIR $DITA_HOME
ENTRYPOINT ["/opt/app/bin/dita"]
