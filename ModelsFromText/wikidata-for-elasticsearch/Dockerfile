FROM docker.elastic.co/elasticsearch/elasticsearch:7.4.0 AS builder
COPY --chown=elasticsearch:elasticsearch elasticsearch.yml /usr/share/elasticsearch/config/

# Install Pip package first
RUN yum -y update \
    && yum --enablerepo=extras -y install epel-release \
    && yum -y install python-pip

# If running behind corporate proxy, use the following instead
#RUN export http_proxy=<INSERT HTTP PROXY HERE> && export https_proxy=<INSERT HTTPS PROXY HERE> \
#        && yum -y update \
#        && yum --enablerepo=extras -y install epel-release \
#        && yum -y install python-pip

# Next, install Elastic-search loader package
RUN pip install elasticsearch-loader

# If running behind corporate proxy, use the following instead
#RUN pip install --proxy=<INSERT HTTP PROXY HERE> elasticsearch-loader

# Add wikidata extract and loading script to image
ADD load.sh /tmp/load.sh
ADD all-terms-wikidata.json /tmp/wikidata.json

# Launch container in detached mode and load wikidata extract into Elasticsearch index called 'wikidata'
RUN time /usr/local/bin/docker-entrypoint.sh elasticsearch -d -E path.data=/tmp/data -E path.logs=/tmp/logs \
		&& while [[ "$(curl --noproxy '*' -s -o /dev/null -w '%{http_code}' localhost:9200)" != "200" ]]; do sleep 2; done \
    		&& chmod +x /tmp/load.sh \
		&& /tmp/load.sh

# Build image of elastic search with data path pre-populated as above
FROM docker.elastic.co/elasticsearch/elasticsearch:7.4.0
COPY --chown=elasticsearch:elasticsearch elasticsearch.yml /usr/share/elasticsearch/config/
COPY --from=builder /tmp/data/ /esdata/
