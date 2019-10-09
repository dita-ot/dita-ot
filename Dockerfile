FROM adoptopenjdk:11-jre-hotspot

RUN apt-get update \
    && apt-get install -y unzip \
    && rm -rf /var/lib/apt/lists/*

ARG VERSION
RUN curl -sLo /tmp/dita-ot-$VERSION.zip https://github.com/dita-ot/dita-ot/releases/download/$VERSION/dita-ot-$VERSION.zip && \
    unzip /tmp/dita-ot-$VERSION.zip -d /tmp/ && \
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
